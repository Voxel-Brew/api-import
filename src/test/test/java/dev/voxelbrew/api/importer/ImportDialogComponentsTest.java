package dev.voxelbrew.api.importer;

import static org.assertj.core.api.Assertions.*;

import dev.voxelbrew.api.importer.component.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ImportDialogComponentsTest {
    @Test
    void nestedLayoutsKeepAnImmutableSnapshotOfChildren() {
        var children = new ArrayList<ImportDialogComponent>();
        children.add(new TextField("name", "Name", "draft"));
        var row = new HorizontalLayout(children);
        var column = new VerticalLayout(row, new Separator(), new Label("Done"));
        children.clear();

        assertThat(row.children()).hasSize(1);
        assertThat(column.children()).hasSize(3);
        assertThat(column.children().getFirst()).isSameAs(row);
        assertThatThrownBy(() -> row.children().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new VerticalLayout(new Label("ok"), null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void selectionInputsRequireAValidDefaultAndCopyChoices() {
        var choices = new ArrayList<>(List.of("draft", "final"));
        var radio = new RadioButtonGroup("stage", "Stage", choices, "draft");
        choices.add("archived");
        assertThat(radio.choices()).containsExactly("draft", "final");
        assertThatThrownBy(() -> radio.choices().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> new RadioButtonGroup("stage", "Stage", List.of("draft"), "final"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ComboBox("stage", "Stage", List.of(), "draft"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sliderRejectsNonFiniteOrOutOfRangeValues() {
        assertThatThrownBy(() -> new Slider("scale", "Scale", 0, 1, Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Slider("scale", "Scale", 1, 1, 1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Slider("scale", "Scale", 0, 1, -0.1)).isInstanceOf(IllegalArgumentException.class);
        assertThat(new Slider("scale", "Scale", 0, 1, 0.5).value()).isEqualTo(0.5);
    }
}
