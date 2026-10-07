package ru.anisim.pdfimposition;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

public class ImpositionWindow {

    private final JFrame frame;

    public ImpositionWindow() {
        frame = new JFrame("Спуск PDF");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 450);
        frame.setLocationRelativeTo(null);

        var panel = new JPanel(new BorderLayout());

        var title = new JLabel(
                "Подготовка спуска А5, А6 и А7",
                SwingConstants.CENTER
        );

        panel.add(title, BorderLayout.CENTER);
        frame.setContentPane(panel);
    }

    public void show() {
        frame.setVisible(true);
    }
}
