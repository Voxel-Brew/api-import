package dev.voxelbrew.importer.api;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
class ImportDialogsTest {
    @lombok.Getter
    @lombok.experimental.Accessors(fluent = true)
    static final class CloudDialog extends ImportDialog<CloudSettings> {
        private final String title;
        private final List<String> projects;

        CloudDialog(String title, List<String> projects) {
            this.title = title;
            this.projects = List.copyOf(projects);
        }
    }

    record CloudSettings(String project, boolean shared) {}

    record CloudReceipt(String resourceId, String project) {}

    @Test
    void providerDefinedCloudInteractionAndResultNeedNoLocalFile() throws Exception {
        var cli = new ImportDialogs().register(CloudDialog.class, request -> {
            assertThat(request.projects()).isEqualTo(List.of("team", "personal"));
            return Optional.of(new CloudSettings("team", true));
        });
        // A provider can collect its own settings and return its own receipt type.
        // No network call is needed to exercise the interaction contract.
        Importer<CloudReceipt> importer = parent -> {
            var settings = parent.show(new CloudDialog("Publish design", List.of("team", "personal")));
            return settings.map(
                            value -> CompletableFuture.completedFuture(new CloudReceipt("remote-id", value.project())))
                    .orElseGet(() -> CompletableFuture.failedFuture(new CancellationException()));
        };
        assertThat(importer.importFrom(cli).get()).isEqualTo(new CloudReceipt("remote-id", "team"));
        var cancelled = new ImportDialogs().register(CloudDialog.class, request -> Optional.empty());
        assertThatThrownBy(() -> importer.importFrom(cancelled).join()).isInstanceOf(CancellationException.class);
    }

    @Test
    void unsupportedDialogsAndDuplicateHandlersFailExplicitly() {
        var host = new ImportDialogs();
        var dialog = new CloudDialog("Publish", List.of("team"));
        assertThatThrownBy(() -> host.show(dialog)).isInstanceOf(UnsupportedOperationException.class);
        host.register(CloudDialog.class, request -> Optional.empty());
        assertThatThrownBy(() ->
                        host.register(CloudDialog.class, request -> Optional.of(new CloudSettings("team", false))))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(host.show(dialog).isEmpty()).isTrue();
    }

    @Test
    void settingsAreAvailableOnlyAfterConfirmationAndReplacedOnReuse() {
        var dialog = new CloudDialog("Publish", List.of("team", "personal"));
        assertThatThrownBy(dialog::getSettings).isInstanceOf(IllegalStateException.class);
        var first = new CloudSettings("team", true);
        var second = new CloudSettings("personal", false);
        var host = new ImportDialogs().register(CloudDialog.class, request -> {
            assertThatThrownBy(request::getSettings).isInstanceOf(IllegalStateException.class);
            return Optional.of(first);
        });
        assertThat(host.show(dialog)).isEqualTo(Optional.of(first));
        assertThat(dialog.getSettings()).isSameAs(first);
        new ImportDialogs()
                .register(CloudDialog.class, request -> Optional.of(second))
                .show(dialog);
        assertThat(dialog.getSettings()).isSameAs(second);
        assertThat(new ImportDialogs()
                        .register(CloudDialog.class, request -> Optional.empty())
                        .show(dialog)
                        .isEmpty())
                .isTrue();
        assertThatThrownBy(dialog::getSettings).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void failedOrUnsupportedInteractionsClearPreviouslyConfirmedSettings() {
        var dialog = new CloudDialog("Publish", List.of("team"));
        var success = new ImportDialogs()
                .register(CloudDialog.class, request -> Optional.of(new CloudSettings("team", false)));
        var failure = new IllegalArgumentException("Invalid account");
        var failing = new ImportDialogs().register(CloudDialog.class, request -> {
            throw failure;
        });
        success.show(dialog);
        assertThat(org.assertj.core.api.Assertions.catchThrowableOfType(
                        () -> failing.show(dialog), IllegalArgumentException.class))
                .isSameAs(failure);
        assertThatThrownBy(dialog::getSettings).isInstanceOf(IllegalStateException.class);
        success.show(dialog);
        assertThatThrownBy(() -> new ImportDialogs().show(dialog)).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(dialog::getSettings).isInstanceOf(IllegalStateException.class);
        success.show(dialog);
        assertThatThrownBy(() -> new ImportDialogs()
                        .register(CloudDialog.class, request -> null)
                        .show(dialog))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(dialog::getSettings).isInstanceOf(IllegalStateException.class);
    }
}
