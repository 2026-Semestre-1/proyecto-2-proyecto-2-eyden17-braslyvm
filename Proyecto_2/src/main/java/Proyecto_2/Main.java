/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Proyecto_2;

/**
 *
 * @author braslyvm
 */


import UI.Interfaz;
import javax.swing.JFrame;

public class Main {
    public static void main(String[] args) {
        // Crear ventana
        JFrame frame = new JFrame("Interfaz");

        // Agregar tu JPanel
        frame.setContentPane(new Interfaz());

        // Configuración básica
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); // ajusta al tamaño del JPanel
        frame.setLocationRelativeTo(null); // centrar en pantalla

        // Mostrar
        frame.setVisible(true);
    }
}