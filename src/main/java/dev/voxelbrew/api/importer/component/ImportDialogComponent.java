package dev.voxelbrew.api.importer.component;

/** UI-neutral component in an importer dialog. */
public sealed interface ImportDialogComponent permits ImportDialogLayout, ImportDialogInput, Label, Separator {}
