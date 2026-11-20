package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.i18n.TranslationService;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Test host that serves the API's registered messages. */
public final class TestTranslationService implements TranslationService {
    private final Map<Locale, Map<String, String>> catalogs;

    public TestTranslationService() {
        this(new ImportTranslationProvider().getTranslations());
    }

    TestTranslationService(Map<Locale, Map<String, String>> catalogs) {
        this.catalogs = catalogs;
    }

    @Override
    public Locale currentLocale() {
        return Locale.getDefault(Locale.Category.DISPLAY);
    }

    @Override
    public Set<Locale> supportedLocales() {
        return Set.of(currentLocale());
    }

    @Override
    public Map<String, String> languages() {
        return Map.of(currentLocale().toLanguageTag(), currentLocale().getDisplayName());
    }

    @Override
    public void addSupportedLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> translate(String key, Object... arguments) {
        var locale = Locale.forLanguageTag(currentLocale().getLanguage());
        var catalog = catalogs.getOrDefault(locale, catalogs.getOrDefault(Locale.ENGLISH, Map.of()));
        return Optional.ofNullable(catalog.get(key))
                .map(pattern -> arguments.length == 0 ? pattern : new MessageFormat(pattern, locale).format(arguments));
    }

    @Override
    public String registerTranslation(Locale locale, String key, String text) {
        throw new UnsupportedOperationException();
    }
}
