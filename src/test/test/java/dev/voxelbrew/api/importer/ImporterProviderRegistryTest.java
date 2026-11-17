package dev.voxelbrew.api.importer;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ImporterProviderRegistryTest {
    @TempDir
    Path plugins;

    public static final class FirstProvider implements ImporterProvider {
        @Override
        public ImporterDescriptor descriptor() {
            return new ImporterDescriptor("first", "First", "1.0.0");
        }

        @Override
        public Importer create() {
            return window -> Optional.empty();
        }
    }

    public static final class SecondProvider implements ImporterProvider {
        @Override
        public ImporterDescriptor descriptor() {
            return new ImporterDescriptor("second", "Second", "2.0.0");
        }

        @Override
        public Importer create() {
            return window -> Optional.empty();
        }
    }

    @Test
    void discoversEveryRegisteredClasspathService() throws Exception {
        var registry =
                ServiceLoader.load(ImporterProviderRegistry.class).findFirst().orElseThrow();
        assertThat(registry).isInstanceOf(ServiceLoaderImporterProviderRegistry.class);
        assertThat(registry.descriptors().stream().map(ImporterDescriptor::id).toList())
                .containsExactly("first", "second");
        assertThat(registry.require("first")).isInstanceOf(FirstProvider.class);
        assertThat(registry.require("first").descriptor().version()).isEqualTo("1.0.0");
        assertThat(registry.require("second")).isInstanceOf(SecondProvider.class);
    }

    @Test
    void reusesLoadedProvidersAcrossLookups() {
        var registry =
                ServiceLoader.load(ImporterProviderRegistry.class).findFirst().orElseThrow();
        assertThat(registry.require("first")).isSameAs(registry.require("first"));
    }

    @Test
    void configuresTheServiceLoadedRegistry() throws Exception {
        try (var registry = ServiceLoader.load(ImporterProviderRegistry.class)
                .findFirst()
                .orElseThrow()
                .withDirectory(plugins)) {
            assertThat(registry.require("first").descriptor().name()).isEqualTo("First");
            assertThat(registry.descriptors().stream()
                            .map(ImporterDescriptor::id)
                            .toList())
                    .containsExactly("first", "second");
        }
    }

    @Test
    void suppliedProviderWinsDuplicateIdAndDescriptorsAreReadOnly() throws Exception {
        var supplied = new FirstProvider();
        try (var registry = new ServiceLoaderImporterProviderRegistry(plugins, List.of(supplied))) {
            assertThat(registry.require("first")).isSameAs(supplied);
            assertThat(registry.descriptors().stream()
                            .map(ImporterDescriptor::id)
                            .toList())
                    .containsExactly("first", "second");
            assertThatThrownBy(() -> registry.descriptors().clear()).isInstanceOf(UnsupportedOperationException.class);
            assertThatThrownBy(() -> registry.require("missing"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("first");
        }
    }

    @Test
    void rejectsNonDirectoryPluginPathAndCannotReconfigureAfterClose() throws Exception {
        var file = Files.writeString(plugins.resolve("not-a-directory"), "contents");
        assertThatThrownBy(() -> new ServiceLoaderImporterProviderRegistry(file))
                .isInstanceOf(IOException.class);

        var registry = new ServiceLoaderImporterProviderRegistry();
        registry.close();
        assertThatCode(registry::close).doesNotThrowAnyException();
        assertThatThrownBy(() -> registry.withDirectory(plugins)).isInstanceOf(IllegalStateException.class);
    }
}
