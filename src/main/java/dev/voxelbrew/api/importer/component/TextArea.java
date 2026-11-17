package dev.voxelbrew.api.importer.component;

import lombok.NonNull;

/** A multi-line editable value. */
public record TextArea(
        @NonNull String id, @NonNull String label, @NonNull String value) implements ImportDialogInput {}
