package dev.voxelbrew.api.importer;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;

/** Discovers classpath and installed importers and owns their plugin class loaders. */
@Slf4j
public final class ServiceLoaderImporterProviderRegistry implements ImporterProviderRegistry {
    private record Jar(Path path, long size, FileTime modified) {}

    private final ClassLoader parent;
    private final List<ImporterProvider> supplied;
    private final List<ImporterProvider> classpath = new ArrayList<>();
    private final Map<String, ImporterProvider> entries = new LinkedHashMap<>();
    private final List<URLClassLoader> loaders = new ArrayList<>();
    private List<Jar> loadedJars;
    private Path directory;
    private boolean closed;

    public ServiceLoaderImporterProviderRegistry() {
        this(ServiceLoaderImporterProviderRegistry.class.getClassLoader(), List.of());
    }

    public ServiceLoaderImporterProviderRegistry(ClassLoader loader) {
        this(loader, List.of());
    }

    public ServiceLoaderImporterProviderRegistry(Path directory) throws IOException {
        this();
        withDirectory(directory);
    }

    public ServiceLoaderImporterProviderRegistry(Path directory, Collection<ImporterProvider> supplied)
            throws IOException {
        this(ServiceLoaderImporterProviderRegistry.class.getClassLoader(), supplied);
        withDirectory(directory);
    }

    private ServiceLoaderImporterProviderRegistry(ClassLoader loader, Collection<ImporterProvider> supplied) {
        parent = Objects.requireNonNull(loader, "loader");
        this.supplied = List.copyOf(supplied);
        var iterator = ServiceLoader.load(ImporterProvider.class, parent).iterator();
        while (iterator.hasNext()) {
            try {
                var provider = iterator.next();
                var descriptor = provider.descriptor();
                if (descriptor == null) continue;
                classpath.add(provider);
                log.info(
                        "Loaded importer {} ({}) version {}", descriptor.name(), descriptor.id(), descriptor.version());
            } catch (ServiceConfigurationError | LinkageError | RuntimeException | AssertionError failure) {
                log.warn("Provider load failed: ", failure);
            }
        }
        try {
            refresh();
        } catch (IOException failure) {
            throw new IllegalStateException("Could not initialize importer registry", failure);
        }
    }

    @Override
    public synchronized ImporterProviderRegistry withDirectory(Path directory) throws IOException {
        if (closed) throw new IllegalStateException("Importer registry is closed");
        this.directory = Objects.requireNonNull(directory, "directory");
        loadedJars = null;
        refresh();
        return this;
    }

    private synchronized void refresh() throws IOException {
        if (closed) throw new IllegalStateException("Importer registry is closed");
        var jars = new ArrayList<Jar>();
        if (directory != null && Files.exists(directory)) {
            if (!Files.isDirectory(directory)) throw new IOException("Plugin path is not a directory: " + directory);
            try (var paths = Files.list(directory)) {
                for (var jar : paths.filter(
                                p -> Files.isRegularFile(p) && p.toString().endsWith(".jar"))
                        .sorted()
                        .toList()) {
                    jars.add(new Jar(jar, Files.size(jar), Files.getLastModifiedTime(jar)));
                }
            }
        }
        if (jars.equals(loadedJars)) return;
        entries.clear();
        for (var provider : supplied) {
            try {
                register(provider);
            } catch (RuntimeException | LinkageError | AssertionError failure) {
                log.warn("Supplied importer failed", failure);
            }
        }
        for (var provider : classpath) {
            try {
                register(provider);
            } catch (RuntimeException | LinkageError | AssertionError failure) {
                log.warn("Provider registration failed", failure);
            }
        }
        if (directory != null) {
            var urls = new ArrayList<URL>();
            for (var jar : jars) urls.add(jar.path().toUri().toURL());
            var loader = new URLClassLoader(urls.toArray(URL[]::new), parent);
            loaders.add(loader);
            discoverPlugins(loader);
        }
        loadedJars = List.copyOf(jars);
    }

    private void discoverPlugins(URLClassLoader loader) {
        var iterator = ServiceLoader.load(ImporterProvider.class, loader).iterator();
        int failures = 0;
        while (true) {
            try {
                if (!iterator.hasNext()) break;
                var provider = iterator.next();
                if (provider.getClass().getClassLoader() != loader) continue;
                register(provider);
                var descriptor = provider.descriptor();
                log.info(
                        "Loaded importer {} ({}) version {}", descriptor.name(), descriptor.id(), descriptor.version());
                failures = 0;
            } catch (ServiceConfigurationError | LinkageError | RuntimeException | AssertionError failure) {
                log.warn("Provider load failed", failure);
                if (++failures >= 100) {
                    log.warn("Stopped after 100 consecutive provider failures");
                    break;
                }
            }
        }
    }

    private void register(ImporterProvider provider) {
        var descriptor = Objects.requireNonNull(provider.descriptor(), "descriptor");
        if (entries.putIfAbsent(descriptor.id(), provider) != null)
            log.warn("Duplicate importer ID: {}", descriptor.id());
    }

    @Override
    public synchronized List<ImporterDescriptor> descriptors() {
        return entries.values().stream()
                .map(ImporterProvider::descriptor)
                .sorted(Comparator.comparing(ImporterDescriptor::id))
                .toList();
    }

    @Override
    public synchronized ImporterProvider require(String id) {
        var provider = entries.get(id);
        if (provider == null)
            throw new IllegalArgumentException("Unknown importer '" + id + "'. Available: " + entries.keySet());
        return provider;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) return;
        closed = true;
        IOException failure = null;
        for (var loader : loaders) {
            try {
                loader.close();
            } catch (IOException error) {
                if (failure == null) failure = error;
                else failure.addSuppressed(error);
            }
        }
        if (failure != null) throw failure;
    }
}
