package dev.voxelbrew.api.importer;

import lombok.NonNull;

/**
 * Immutable discovery metadata shared by file, model and remote-service importers.
 *
 * <p>The ID is used for programmatic selection and must remain stable across releases. Choose an ID
 * distinct from built-ins and other plugins; duplicate IDs do not create separate menu entries.
 * File extensions, settings and result types belong to the implementation, not this descriptor.
 *
 * @param id nonempty identifier matching {@code [a-z0-9][a-z0-9.-]*}, for example {@code cloud-model}
 * @param name nonblank display label used by the host, for example {@code Cloud model upload}
 * @param version nonblank version of the importer implementation shown by the host
 */
public record ImporterDescriptor(
        String id, @NonNull String name, @NonNull String version) {
    /**
     * Creates and validates discovery metadata without normalizing either value.
     *
     * @param id stable importer identifier
     * @param name human-readable display label
     * @param version importer implementation version
     * @throws IllegalArgumentException if the ID is null or invalid, or the name or version is blank
     * @throws NullPointerException if the name or version is null
     */
    public ImporterDescriptor {
        if (id == null || !id.matches("[a-z0-9][a-z0-9.-]*")) throw new IllegalArgumentException("Invalid importer ID");
        if (name.isBlank()) throw new IllegalArgumentException("Missing importer name");
        if (version.isBlank()) throw new IllegalArgumentException("Missing importer version");
    }
}
