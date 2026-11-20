package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.i18n.PropertiesTranslationProvider;
import java.util.List;
import java.util.Locale;

public final class ImportTranslationProvider extends PropertiesTranslationProvider {
    public ImportTranslationProvider() {
        super("dev.voxelbrew.api.importer.messages");
    }

    @Override
    protected List<Locale> getLocales() {
        return List.of(Locale.ENGLISH, Locale.forLanguageTag("pl"));
    }
}
