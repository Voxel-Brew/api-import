package dev.voxelbrew.api.importer.component;

import java.util.List;

/** Arranges child components in a row. */
public record HorizontalLayout(List<ImportDialogComponent> children) implements ImportDialogLayout {
    public HorizontalLayout(ImportDialogComponent... children) {
        this(List.of(children));
    }

    public HorizontalLayout {
        children = List.copyOf(children);
    }
}
