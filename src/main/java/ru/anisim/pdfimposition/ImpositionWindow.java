package ru.anisim.pdfimposition;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.nio.file.Path;

public class ImpositionWindow {

    private final JFrame frame;
    private final JComboBox<String> formatBox;
    private final DefaultListModel<Path> filesModel;

    public ImpositionWindow() {
        frame = new JFrame("Спуск PDF");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);

        formatBox = new JComboBox<>(new String[]{
                "А5 — 4 копии",
                "А6 — 8 копий",
                "А7 — 18 копий"
        });

        filesModel = new DefaultListModel<>();
        var filesList = new JList<>(filesModel);

        var addButton = new JButton("Добавить PDF");
        addButton.addActionListener(event -> chooseFiles());

        var clearButton = new JButton("Очистить список");
        clearButton.addActionListener(event -> filesModel.clear());

        var controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Формат:"));
        controls.add(formatBox);
        controls.add(addButton);
        controls.add(clearButton);

        var panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        panel.add(controls, BorderLayout.NORTH);
        panel.add(new JScrollPane(filesList), BorderLayout.CENTER);

        frame.setContentPane(panel);
    }

    public void show() {
        frame.setVisible(true);
    }

    private void chooseFiles() {
        var chooser = new JFileChooser();

        chooser.setDialogTitle("Выберите PDF одного формата");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(
                new FileNameExtensionFilter("PDF-файлы", "pdf")
        );
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(frame)
                != JFileChooser.APPROVE_OPTION) {
            return;
        }

        for (var file : chooser.getSelectedFiles()) {
            var path = file.toPath().toAbsolutePath().normalize();

            if (!filesModel.contains(path)) {
                filesModel.addElement(path);
            }
        }
    }
}
