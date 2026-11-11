package co.edu.umb.cajero;

import co.edu.umb.cajero.servicio.CajeroService;
import co.edu.umb.cajero.servicio.RepositorioCuentas;
import co.edu.umb.cajero.vista.VentanaCajero;

import javax.swing.*;
import java.nio.file.Path;

/**
 * Punto de entrada de la aplicación Cajero Automático.
 * Los datos se guardan en la carpeta "datos" junto al lugar donde se ejecuta el programa.
 */
public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se usa el aspecto por defecto
        }
        SwingUtilities.invokeLater(() -> {
            try {
                RepositorioCuentas repositorio = new RepositorioCuentas(Path.of("datos"));
                CajeroService cajero = new CajeroService(repositorio);
                new VentanaCajero(cajero).setVisible(true);
            } catch (IllegalStateException e) {
                JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
