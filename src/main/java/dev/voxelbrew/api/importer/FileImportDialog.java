package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.model.vui.FileSource;
import dev.voxelbrew.api.model.vui.Layout;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import lombok.Getter;
import lombok.NonNull;

/** Standard interaction for choosing one local source file. */
@lombok.experimental.Accessors(fluent = true)
public final class FileImportDialog implements ImportDialog<FileImportDialog.Source> {
    @Getter
    private final String title;

    @Getter
    private final String extension;

    @Getter
    private final FileSource sourceInput;

    /** Accepted path, retained exactly as supplied. */
    public record Source(@NonNull Path path) {}

    public FileImportDialog(@NonNull String title, String extension) {
        this.title = title;
        if (extension == null || !extension.matches("[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*"))
            throw new IllegalArgumentException("Invalid extension");
        this.extension = extension;
        sourceInput = new FileSource("source", extension);
    }

    @Override
    public Layout layout() {
        return new VerticalLayout(sourceInput);
    }

    @Override
    public Map<String, String> validate(Map<String, String> values) {
        var supplied = values.get(sourceInput.id());
        if (supplied == null || supplied.isBlank()) return Map.of(sourceInput.id(), "Choose a source");
        try {
            var path = Path.of(supplied);
            if (!Files.exists(path)) return Map.of(sourceInput.id(), "Source does not exist");
            if (!Files.isRegularFile(path)) return Map.of(sourceInput.id(), "Source must be a file");
            if (!Files.isReadable(path)) return Map.of(sourceInput.id(), "Source is not readable");
        } catch (InvalidPathException failure) {
            return Map.of(sourceInput.id(), "Invalid source path: " + failure.getReason());
        }
        return Map.of();
    }

    @Override
    public Source buildSettings(Map<String, String> values) {
        var errors = validate(values);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors.values()));
        return new Source(Path.of(values.get(sourceInput.id())));
    }
}
