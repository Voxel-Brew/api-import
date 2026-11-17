package dev.voxelbrew.api.importer;

import dev.voxelbrew.api.model.CadDocument;
import java.util.Optional;

/** Imports a detached editable document using importer-owned interaction and input reading. */
@FunctionalInterface
public interface Importer {
    /** Returns an imported document, or empty when the user cancels before reading. */
    Optional<CadDocument> importFrom(ImportWindow window) throws Exception;
}
