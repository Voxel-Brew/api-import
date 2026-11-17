package dev.voxelbrew.api.importer.component;

import java.nio.file.Path;
import lombok.NonNull;

/**
 * File chooser source with a suggested extension and optional initial path.
 *
 * @param id input ID used by the host
 * @param extension suggested file extension
 * @param defaultPath initial path, or {@code null} to let the host choose or prompt
 */
public record FileSource(@NonNull String id, @NonNull String extension, Path defaultPath) implements ImportDialogInput {
    /** Creates a source input whose initial path is chosen by the host. */
    public FileSource(String id, String extension) {
        this(id, extension, null);
    }
}
