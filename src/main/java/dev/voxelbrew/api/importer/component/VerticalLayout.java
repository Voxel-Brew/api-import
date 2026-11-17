package dev.voxelbrew.api.importer.component;

import java.util.List;

/** Arranges child components in a column. */
public record VerticalLayout(List<ImportDialogComponent> children) implements ImportDialogLayout {
    public VerticalLayout(ImportDialogComponent... children) {
        this(List.of(children));
    }

    public VerticalLayout {
        children = List.copyOf(children);
    }
}
