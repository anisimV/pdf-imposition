package ru.anisim.pdfimposition;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class DesktopWindow {

    public void show() {
        // Создаём и настраиваем окно выбора файлов
        var chooser = new JFileChooser();

        chooser.setDialogTitle("Выберите макеты PDF");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new FileNameExtensionFilter("PDF-файлы", "pdf"));
        chooser.setAcceptAllFileFilterUsed(false);

        var result = chooser.showOpenDialog(null);

        // При отмене завершаем метод
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        // Собираем пути выбранных файлов в список
        List<Path> inputPaths = Arrays.stream(chooser.getSelectedFiles()).map(file -> file.toPath()).toList();

        // Создаём сервисы обработки
       // var pdfService = new PdfImpositionService(ImpositionLayouts.A5);
        var pdfService = new PdfImpositionService(
                ImpositionLayouts.A6,
                5f
        );
        var batchService = new BatchImpositionService(pdfService);

        // Обрабатываем файлы в фоновом потоке
        var worker = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return batchService.process(inputPaths);
            }

            // Показываем результат после завершения обработки
            @Override
            protected void done() {
                try {
                    var results = get();

                    JOptionPane.showMessageDialog(null, String.join("\n", results), "Результат обработки", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(null, "Не удалось выполнить обработку: " + e.getCause().getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }
}
