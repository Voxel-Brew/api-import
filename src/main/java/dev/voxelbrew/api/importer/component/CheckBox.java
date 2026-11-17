package dev.voxelbrew.api.importer.component;

import lombok.NonNull;

/** A boolean option. */
public record CheckBox(@NonNull String id, @NonNull String label, boolean selected) implements ImportDialogInput {}
