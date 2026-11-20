package dev.voxelbrew.api.importer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ImporterDescriptorTest {
    @Test
    void requiresAnImporterVersion() {
        assertThatThrownBy(() -> new ImporterDescriptor("example", "Example", null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new ImporterDescriptor("example", "Example", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(new ImporterDescriptor("example", "Example", "1.2.3").version())
                .isEqualTo("1.2.3");
    }

    @Test
    void rejectsInvalidDiscoveryMetadataWithoutChangingValidValues() {
        for (String id : java.util.List.of("", "Bad", "has space", "_hidden")) {
            assertThatThrownBy(() -> new ImporterDescriptor(id, "Example", "1.0"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new ImporterDescriptor("example", " ", "1.0"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ImporterDescriptor("example", null, "1.0"))
                .isInstanceOf(NullPointerException.class);
        var descriptor = new ImporterDescriptor("example.format", "  Example  ", "v1");
        assertThat(descriptor.id()).isEqualTo("example.format");
        assertThat(descriptor.name()).isEqualTo("  Example  ");
    }
}
