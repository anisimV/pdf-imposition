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

        var formats = new String[]{
                "А5 — 4 копии",
                "А6 — 8 копий",
                "А7 — 18 копий"
        };

        var formatIndex = JOptionPane.showOptionDialog(null, "Выберите формат макетов", "Формат спуска", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, formats, formats[0]);

        if (formatIndex == JOptionPane.CLOSED_OPTION) {
            return;
        }

        var layout = switch (formatIndex) {
            case 0 -> ImpositionLayouts.A5;
            case 1 -> ImpositionLayouts.A6;
            case 2 -> ImpositionLayouts.A7;
            default -> throw new IllegalStateException("Неизвестный формат: " + formatIndex);
        };
        // Создаём и настраиваем окно выбора файлов
        var chooser = new JFileChooser();

        chooser.setDialogTitle("Выберите макеты PDF формата " + layout.name());
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
        var inputBleedMm = switch (formatIndex) {
            case 0 -> 0f;
            case 1 -> 5f;
            case 2 -> 0f;
            default -> throw new IllegalStateException(
                    "Неизвестный формат: " + formatIndex
            );
        };

        var pdfService = new PdfImpositionService(
                layout,
                inputBleedMm
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
