package dev.voxelbrew.api.importer.component;

import java.util.List;
import lombok.NonNull;

/** A single selection from visible choices. */
public record RadioButtonGroup(@NonNull String id, @NonNull String label, List<String> choices, String selected)
        implements ImportDialogInput {
    public RadioButtonGroup {
        choices = List.copyOf(choices);
        if (choices.isEmpty() || !choices.contains(selected))
            throw new IllegalArgumentException("Selected value must be one of the choices");
    }
}
