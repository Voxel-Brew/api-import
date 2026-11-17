package dev.voxelbrew.api.importer;

import java.util.Optional;

/**
 * Host abstraction for importer interactions, shared by desktop and headless workflows.
 *
 * <p>A desktop implementation can retain its actual owner window internally; a CLI implementation
 * can resolve settings from parsed arguments or prompts. Neither requires frontend types in the
 * importer contract. Implementations decide which request types they support and must document any
 * thread restrictions. This interface does not assume a file destination.
 *
 */
public interface ImportWindow {
    /**
     * Resolves an importer-defined request synchronously, returning only after confirmation or
     * cancellation.
     *
     * <p>Return an empty optional for cancellation, not for an unsupported request or invalid
     * settings. Unsupported requests must fail explicitly; validation failures must be corrected or
     * reported to the caller.
     *
     * @param <R> accepted settings type
     * @param dialog request to resolve; must not be {@code null}
     * @return accepted settings, or empty on cancellation
     * @throws UnsupportedOperationException if this host cannot handle the dialog type
     */
    <R> Optional<R> show(ImportDialog<R> dialog);
}
