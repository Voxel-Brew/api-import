package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.i18n.TranslationService;
import dev.voxelbrew.api.model.vui.FileSource;
import dev.voxelbrew.api.model.vui.Layout;
import dev.voxelbrew.api.model.vui.VerticalLayout;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import lombok.Getter;
import lombok.NonNull;

/** Standard interaction for choosing one local source file. */
@lombok.experimental.Accessors(fluent = true)
public final class FileImportDialog implements ImportDialog<FileImportDialog.Source> {
    private final Optional<TranslationService> translations;

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
                        .findFirst());
    }

    /** Creates a file request using the host's translation service. */
    public FileImportDialog(@NonNull String title, String extension, TranslationService translations) {
        this(title, extension, Optional.of(translations));
    }

    private FileImportDialog(@NonNull String title, String extension, Optional<TranslationService> translations) {
        this.translations = translations;
        this.title = title;
        if (extension == null || !extension.matches("[a-zA-Z0-9]+(?:\\.[a-zA-Z0-9]+)*"))
            throw new IllegalArgumentException("Invalid extension");
        this.extension = extension;
        sourceInput = new FileSource("source", extension);
    }

    private String message(boolean localize, String key, Object... arguments) {
        if (localize) {
            var translated = translations.flatMap(service -> service.translate(key, arguments));
            if (translated.isPresent()) return translated.get();
        }
        var display = Locale.getDefault(Locale.Category.DISPLAY);
        var locale = localize && display.getLanguage().equals("pl") ? Locale.forLanguageTag("pl") : Locale.ENGLISH;
        String pattern = ResourceBundle.getBundle("dev.voxelbrew.api.importer.messages", locale)
                .getString(key);
        return arguments.length == 0 ? pattern : new MessageFormat(pattern, locale).format(arguments);
    }

    @Override
    public Layout layout() {
        return new VerticalLayout(sourceInput);
    }

    @Override
    public Map<String, String> validate(Map<String, String> values) {
        return validate(values, true);
    }

    private Map<String, String> validate(Map<String, String> values, boolean localize) {
        var supplied = values.get(sourceInput.id());
        if (supplied == null || supplied.isBlank())
            return Map.of(sourceInput.id(), message(localize, "api.import.source.choose"));
        try {
            var path = Path.of(supplied);
            if (!Files.exists(path)) return Map.of(sourceInput.id(), message(localize, "api.import.source.missing"));
            if (!Files.isRegularFile(path))
                return Map.of(sourceInput.id(), message(localize, "api.import.source.file"));
            if (!Files.isReadable(path))
                return Map.of(sourceInput.id(), message(localize, "api.import.source.readable"));
        } catch (InvalidPathException failure) {
            return Map.of(sourceInput.id(), message(localize, "api.import.source.invalid", failure.getReason()));
        }
        return Map.of();
    }

    @Override
    public Source buildSettings(Map<String, String> values) {
        var errors = validate(values, false);
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors.values()));
        return new Source(Path.of(values.get(sourceInput.id())));
    }
}
