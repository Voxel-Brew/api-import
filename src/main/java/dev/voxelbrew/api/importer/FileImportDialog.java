package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.i18n.TranslationService;
import dev.voxelbrew.api.model.vui.FileSource;
import dev.voxelbrew.api.model.vui.Layout;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/** Standard interaction for choosing one local source file. */
@Slf4j
@lombok.experimental.Accessors(fluent = true)
public final class FileImportDialog implements ImportDialog<FileImportDialog.Source> {
    private final TranslationService translations;

    @Getter
    private final String title;

    @Getter
    private final String extension;

    @Getter
    private final FileSource sourceInput;

    /** Accepted path, retained exactly as supplied. */
    public record Source(@NonNull Path path) {}

    public FileImportDialog(@NonNull String title, String extension) {
        this(
                title,
                extension,
                ServiceLoader.load(TranslationService.class, TranslationService.class.getClassLoader())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("A TranslationService provider is required")));
    }

    /** Creates a file request using the host's translation service. */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "EI_EXPOSE_REP2",
            justification = "The dialog shares the host service to observe its current translations.")
    public FileImportDialog(@NonNull String title, String extension, @NonNull TranslationService translations) {
        this.translations = translations;
        this.title = title;
        if (extension == null || !extension.matches("[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*"))
            throw new IllegalArgumentException("Invalid extension");
        this.extension = extension;
        sourceInput = new FileSource("source", extension);
    }

    private String message(String key, Object... arguments) {
        return translations.translate(key, arguments).orElseGet(() -> {
            log.warn("Missing translation for key {}", key);
            return key;
        });
    }

    @Override
    public Layout layout() {
        return new VerticalLayout(sourceInput);
    }

    @Override
    public Map<String, String> validate(Map<String, String> values) {
        var supplied = values.get(sourceInput.id());
        if (supplied == null || supplied.isBlank())
            return Map.of(sourceInput.id(), message("api.import.source.choose"));
        try {
            var path = Path.of(supplied);
            if (!Files.exists(path)) return Map.of(sourceInput.id(), message("api.import.source.missing"));
            if (!Files.isRegularFile(path)) return Map.of(sourceInput.id(), message("api.import.source.file"));
            if (!Files.isReadable(path)) return Map.of(sourceInput.id(), message("api.import.source.readable"));
        } catch (InvalidPathException failure) {
            return Map.of(sourceInput.id(), message("api.import.source.invalid", failure.getReason()));
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
