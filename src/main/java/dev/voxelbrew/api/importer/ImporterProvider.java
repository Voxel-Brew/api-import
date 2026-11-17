package dev.voxelbrew.api.importer;

/** ServiceLoader entry point for importer implementations. */
public interface ImporterProvider {
    ImporterDescriptor descriptor();

    /** Creates an importer without transferring threading ownership to the plugin. */
    Importer create();
}
