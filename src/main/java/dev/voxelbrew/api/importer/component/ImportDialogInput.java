package dev.voxelbrew.api.importer.component;

/** Component whose value can be supplied by a frontend. */
public sealed interface ImportDialogInput extends ImportDialogComponent
        permits TextField, TextArea, CheckBox, ComboBox, Slider, FileSource, RadioButtonGroup {
    String id();
}
