package ru.anisim.pdfimposition;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;

public class App {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatLightLaf.setup();
            new ImpositionWindow().show();
        });
    }
}
