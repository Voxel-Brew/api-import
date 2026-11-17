package dev.voxelbrew.api.importer.component;

/** A component that arranges child components in a dialog. */
public sealed interface ImportDialogLayout extends ImportDialogComponent permits HorizontalLayout, VerticalLayout {}
