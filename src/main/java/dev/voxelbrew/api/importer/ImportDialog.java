package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.importer.component.ImportDialogLayout;
import java.util.Map;

/** Frontend-neutral description of an importer interaction. */
public interface ImportDialog<R> {
    /** Human-readable dialog title. */
    String title();

    /** Controls and their arrangement, interpreted by the frontend. */
    ImportDialogLayout layout();

    /**
     * Checks the supplied values, which may be incomplete while a host gathers input. Returns all
     * expected errors keyed by {@link dev.voxelbrew.api.importer.component.ImportDialogInput#id()},
     * or an empty map when valid. Each message should explain how to correct the value.
     */
    Map<String, String> validate(Map<String, String> values);

    /** Converts frontend input values, keyed by component ID, into accepted settings. */
    R buildSettings(Map<String, String> values);
}
