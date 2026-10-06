package ru.anisim.pdfimposition;

import javax.swing.SwingUtilities;


public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            var window = new DesktopWindow();
            window.show();
        });
    }
}
