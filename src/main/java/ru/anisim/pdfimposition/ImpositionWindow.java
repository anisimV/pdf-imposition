package ru.anisim.pdfimposition;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JProgressBar;
import java.awt.FileDialog;
import java.util.Locale;

public class ImpositionWindow {

    private final JFrame frame;
    private final JComboBox<String> formatBox;
    private final DefaultListModel<Path> filesModel;
    private final JProgressBar progressBar;

    private final JButton addButton;
    private final JButton clearButton;
    private final JButton processButton;
    private final JLabel statusLabel;

    public ImpositionWindow() {
        frame = new JFrame("Спуск PDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);

        formatBox = new JComboBox<>(new String[]{"А5 — 4 копии", "А6 — 8 копий", "А7 — 18 копий"});

        filesModel = new DefaultListModel<>();
        var filesList = new JList<>(filesModel);

        addButton = new JButton("Добавить PDF");
        addButton.addActionListener(event -> chooseFiles());

        clearButton = new JButton("Очистить список");
        clearButton.addActionListener(event -> filesModel.clear());

        processButton = new JButton("Создать спуски");
        processButton.addActionListener(event -> processFiles());

        statusLabel = new JLabel("Добавьте PDF и выберите формат.");

        var controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Формат:"));
        controls.add(formatBox);
        controls.add(addButton);
        controls.add(clearButton);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Ожидание");

        var progressPanel = new JPanel(new BorderLayout(0, 5));
        progressPanel.add(statusLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        var bottomPanel = new JPanel(new BorderLayout(10, 0));
        bottomPanel.add(progressPanel, BorderLayout.CENTER);
        bottomPanel.add(processButton, BorderLayout.EAST);

        var panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(filesList), BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        frame.setContentPane(panel);
    }

    public void show() {
        frame.setVisible(true);
    }

    private void chooseFiles() {
        var desktop = FileSystemView.getFileSystemView().getHomeDirectory();

        var dialog = new FileDialog(frame, "Выберите PDF одного формата", FileDialog.LOAD);

        dialog.setDirectory(desktop.getAbsolutePath());
        dialog.setMultipleMode(true);

        try {
            dialog.setVisible(true);

            var selectedFiles = dialog.getFiles();
            var skippedFiles = new ArrayList<String>();

            for (var file : selectedFiles) {
                var fileName = file.getName().toLowerCase(Locale.ROOT);

                if (!file.isFile() || !fileName.endsWith(".pdf")) {
                    skippedFiles.add(file.getName());
                    continue;
                }

                var path = file.toPath().toAbsolutePath().normalize();

                if (!filesModel.contains(path)) {
                    filesModel.addElement(path);
                }
            }

            if (!skippedFiles.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Добавлены только PDF. Пропущены:\n" + String.join("\n", skippedFiles), "Выбор файлов", JOptionPane.WARNING_MESSAGE);
            }
        } finally {
            dialog.dispose();
        }
    }

    private Path chooseDestinationDirectory() {
        var desktop = FileSystemView.getFileSystemView().getHomeDirectory();

        var chooser = new JFileChooser(desktop);

        chooser.setDialogTitle("Выберите папку, в которой создать папку «спуски»");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        return chooser.getSelectedFile().toPath().toAbsolutePath().normalize();
    }

    private void setProcessing(boolean processing) {
        formatBox.setEnabled(!processing);
        addButton.setEnabled(!processing);
        clearButton.setEnabled(!processing);
        processButton.setEnabled(!processing);
    }

    private void processFiles() {
        if (filesModel.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Сначала добавьте PDF-файлы.", "Файлы не выбраны", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Одна папка назначения для всей партии.
        var destinationDirectory = chooseDestinationDirectory();

        if (destinationDirectory == null) {
            return;
        }

        // Копируем пути до запуска фоновой обработки.
        var selectedPaths = new ArrayList<Path>();

        for (var index = 0; index < filesModel.size(); index++) {
            selectedPaths.add(filesModel.getElementAt(index));
        }

        var inputPaths = List.copyOf(selectedPaths);
        var formatIndex = formatBox.getSelectedIndex();

        var layout = switch (formatIndex) {
            case 0 -> ImpositionLayouts.A5;
            case 1 -> ImpositionLayouts.A6;
            case 2 -> ImpositionLayouts.A7;
            default -> throw new IllegalStateException("Неизвестный формат: " + formatIndex);
        };

        var inputBleedMm = formatIndex == 1 ? 5f : 0f;

        var pdfService = new PdfImpositionService(layout, inputBleedMm);
        var batchService = new BatchImpositionService(pdfService);

        setProcessing(true);
        statusLabel.setText("Обработка " + layout.name() + ": файлов — " + inputPaths.size());

        var worker = new SwingWorker<List<String>, Integer>() {

            @Override
            protected List<String> doInBackground() {
                return batchService.process(inputPaths, destinationDirectory, completed -> publish(completed));
            }

            @Override
            protected void process(List<Integer> chunks) {
                var completed = chunks.get(chunks.size() - 1);

                progressBar.setValue(completed);
                progressBar.setString("Обработано " + completed + " из " + inputPaths.size());

                statusLabel.setText("Обработка " + layout.name() + ": " + completed + " из " + inputPaths.size());
            }

            @Override
            protected void done() {
                try {
                    var results = get();

                    progressBar.setValue(inputPaths.size());
                    progressBar.setString("Обработано " + inputPaths.size() + " из " + inputPaths.size());

                    statusLabel.setText("Обработка завершена. Проверьте результаты.");

                    var resultsText = "Папка результатов: " + destinationDirectory.resolve("спуски") + "\n\n" + String.join("\n", results);

                    var resultsArea = new JTextArea(resultsText, 12, 55);

                    resultsArea.setEditable(false);
                    resultsArea.setCaretPosition(0);

                    JOptionPane.showMessageDialog(frame, new JScrollPane(resultsArea), "Результаты обработки", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                    statusLabel.setText("Не удалось получить результаты обработки.");
                } catch (ExecutionException e) {
                    statusLabel.setText("Ошибка обработки.");

                    JOptionPane.showMessageDialog(frame, "Не удалось выполнить обработку: " + e.getCause(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                } finally {
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(inputPaths.size());
                    progressBar.setValue(0);
                    progressBar.setString("Обработано 0 из " + inputPaths.size());
                    setProcessing(false);
                }
            }
        };

        worker.execute();
    }
}
