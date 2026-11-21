package dev.voxelbrew.api.importer;

import static org.assertj.core.api.Assertions.*;

import dev.voxelbrew.api.model.vui.FileSource;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileImportDialogTest {
    @TempDir
    Path directory;

    @Test
    void returnsDiagnosticTextWhenTheHostHasNoTranslation() {
        var dialog = new FileImportDialog("Test", "vxb", new TestTranslationService(Map.of()));
        String expected = Locale.getDefault(Locale.Category.DISPLAY) + ":api.import.source.choose[]";
        assertThat(dialog.validate(Map.of())).containsEntry("source", expected);
        assertThatThrownBy(() -> dialog.buildSettings(Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(expected);
    }

    @Test
    void localizesValidationAndExceptionMessages() {
        Locale previous = Locale.getDefault(Locale.Category.DISPLAY);
        try {
            Locale.setDefault(Locale.Category.DISPLAY, Locale.forLanguageTag("pl"));
            var dialog = new FileImportDialog("Open", "vxb");
            assertThat(dialog.validate(Map.of())).containsEntry("source", "Wybierz plik źródłowy");
            assertThatThrownBy(() -> dialog.buildSettings(Map.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Wybierz plik źródłowy");
        } finally {
            Locale.setDefault(Locale.Category.DISPLAY, previous);
        }
    }

    @Test
    void describesAFileSourceAndPreservesTheAcceptedPath() throws Exception {
        var dialog = new FileImportDialog("Open model", "vxb");
        assertThat(dialog.title()).isEqualTo("Open model");
        assertThat(((VerticalLayout) dialog.layout()).children()).containsExactly(dialog.sourceInput());
        assertThat(dialog.sourceInput()).isEqualTo(new FileSource("source", "vxb"));
        var file = Files.writeString(directory.resolve("model with spaces.txt"), "model");
        var values = Map.of("source", file.toString());
        assertThat(dialog.validate(values)).isEmpty();
        assertThat(dialog.buildSettings(values).path()).isEqualTo(file);
        assertThat(dialog.extension()).isEqualTo("vxb");
    }

    @Test
    void rejectsInvalidSourcesAndAllowsAnyFilenameExtension() throws Exception {
        var dialog = new FileImportDialog("Open", "vxb");
        assertThat(dialog.validate(Map.of())).containsKey("source");
        assertThat(dialog.validate(Map.of("source", " "))).containsKey("source");
        assertThat(dialog.validate(Map.of("source", "\u0000"))).containsKey("source");
        assertThat(dialog.validate(Map.of("source", directory.resolve("missing").toString())))
                .containsKey("source");
        assertThat(dialog.validate(Map.of("source", directory.toString()))).containsKey("source");
        var file = Files.writeString(directory.resolve("other.txt"), "data");
        assertThat(dialog.buildSettings(Map.of("source", file.toString())).path())
                .isEqualTo(file);
        assertThatThrownBy(() -> dialog.buildSettings(Map.of())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new FileImportDialog("Open", "../vxb")).isInstanceOf(IllegalArgumentException.class);
    }
}
