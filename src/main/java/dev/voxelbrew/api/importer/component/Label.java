package dev.voxelbrew.api.importer.component;

import lombok.NonNull;

/** Read-only text. */
public record Label(@NonNull String text) implements ImportDialogComponent {}
