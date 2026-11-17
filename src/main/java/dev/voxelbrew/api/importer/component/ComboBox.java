package dev.voxelbrew.api.importer.component;

import java.util.List;
import lombok.NonNull;

/** A single selection from a drop-down list. */
public record ComboBox(@NonNull String id, @NonNull String label, List<String> choices, String selected)
        implements ImportDialogInput {
    public ComboBox {
        choices = List.copyOf(choices);
        if (choices.isEmpty() || !choices.contains(selected))
            throw new IllegalArgumentException("Selected value must be one of the choices");
    }
}
