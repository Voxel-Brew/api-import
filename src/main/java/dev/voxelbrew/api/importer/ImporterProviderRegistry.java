package dev.voxelbrew.api.importer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Discovers classpath and installed importer providers for a host workflow. */
public interface ImporterProviderRegistry extends AutoCloseable {
    /** Configures plugin discovery on the service-loaded instance. */
    ImporterProviderRegistry withDirectory(Path directory) throws IOException;

    List<ImporterDescriptor> descriptors();

    ImporterProvider require(String id);

    @Override
    void close() throws IOException;
}
