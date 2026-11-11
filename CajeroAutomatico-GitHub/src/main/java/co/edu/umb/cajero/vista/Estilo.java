package co.edu.umb.cajero.vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Colores, fuentes y fábricas de componentes compartidos por todas las pantallas.
 */
final class Estilo {

    static final Color FONDO = new Color(0x0F2A44);
    static final Color PANTALLA = new Color(0xF4F7FA);
    static final Color PRIMARIO = new Color(0x1565C0);
    static final Color PRIMARIO_OSCURO = new Color(0x0D47A1);
    static final Color EXITO = new Color(0x2E7D32);
    static final Color PELIGRO = new Color(0xC62828);
    static final Color TEXTO = new Color(0x1B2631);
    static final Color TEXTO_SUAVE = new Color(0x5D6D7E);

    static final Font TITULO = new Font("SansSerif", Font.BOLD, 24);
    static final Font SUBTITULO = new Font("SansSerif", Font.PLAIN, 15);
    static final Font BOTON = new Font("SansSerif", Font.BOLD, 15);
    static final Font CAMPO = new Font("Monospaced", Font.BOLD, 20);
    static final Font SALDO = new Font("SansSerif", Font.BOLD, 40);

    private Estilo() { }

    static JButton boton(String texto, Color color) {
        JButton b = new JButton(texto);
        b.setFont(BOTON);
        b.setForeground(Color.WHITE);
        b.setBackground(color);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(12, 18, 12, 18));
        return b;
    }

    static JLabel titulo(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(TITULO);
        l.setForeground(TEXTO);
        return l;
    }

    static JLabel subtitulo(String texto) {
        JLabel l = new JLabel(texto, SwingConstants.CENTER);
        l.setFont(SUBTITULO);
        l.setForeground(TEXTO_SUAVE);
        return l;
    }

    static JPanel panelPantalla() {
        JPanel p = new JPanel();
        p.setBackground(PANTALLA);
        p.setBorder(new EmptyBorder(24, 32, 24, 32));
        return p;
    }
}
