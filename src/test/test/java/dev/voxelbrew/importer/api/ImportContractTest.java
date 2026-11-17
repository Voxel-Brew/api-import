package dev.voxelbrew.importer.api;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.Test;

class ImportContractTest {
    @Test
    void malformedDescriptorsAndOptionsFailBeforeReading() {
        var scale = new ImportOption("scale", "Scale", ImportOption.Type.DECIMAL, "1", List.of());
        assertThatThrownBy(() -> scale.validate("NaN")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FileImportDialog("Reader", List.of("txt"), List.of(scale, scale)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FileImportDialog("Reader", List.of("../txt"), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        var options = new ArrayList<>(List.of(scale));
        var descriptor = new FileImportDialog("Reader", List.of("txt"), options);
        options.clear();
        assertThat(descriptor.options().size()).isEqualTo(1);
        assertThatThrownBy(() -> new ImportResult.Document(null)).isInstanceOf(NullPointerException.class);
    }
}
