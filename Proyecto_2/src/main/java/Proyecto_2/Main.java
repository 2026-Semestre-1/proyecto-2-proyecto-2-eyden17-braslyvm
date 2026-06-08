package Proyecto_2;

import UI.Interfaz;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Proyecto 2 - Simulador SO");
            frame.setContentPane(new Interfaz());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(1200, 760));
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            frame.toFront();
            frame.requestFocus();
        });
    }
}