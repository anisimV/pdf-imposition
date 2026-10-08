package ru.anisim.pdfimposition;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.DirectoryChooser;

import javax.swing.SwingUtilities;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

public final class NativeDirectoryChooser {

    public NativeDirectoryChooser() {
        // Инициализируем JavaFX из потока Swing.
        new JFXPanel();

        // Позволяем открывать диалог повторно.
        Platform.setImplicitExit(false);
    }

    public void choose(File initialDirectory, Consumer<Path> onSelected, Consumer<RuntimeException> onError) {
        Platform.runLater(() -> {
            final Path selectedPath;

            try {
                var chooser = new DirectoryChooser();
                chooser.setTitle("Выберите папку для создания папки «спуски»");

                if (initialDirectory != null && initialDirectory.isDirectory()) {
                    chooser.setInitialDirectory(initialDirectory);
                }

                var directory = chooser.showDialog(null);

                selectedPath = directory == null ? null : directory.toPath().toAbsolutePath().normalize();
            } catch (RuntimeException e) {
                SwingUtilities.invokeLater(() -> onError.accept(e));
                return;
            }

            SwingUtilities.invokeLater(() -> onSelected.accept(selectedPath));
        });
    }
}
