package co.edu.umb.cajero.vista;

import co.edu.umb.cajero.modelo.Cuenta;
import co.edu.umb.cajero.modelo.Movimiento;
import co.edu.umb.cajero.modelo.TipoMovimiento;
import co.edu.umb.cajero.servicio.CajeroService;
import co.edu.umb.cajero.servicio.OperacionException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

import static co.edu.umb.cajero.servicio.CajeroService.formatoPesos;

/**
 * Ventana principal del cajero. Usa un CardLayout para cambiar entre pantallas:
 * ingreso, menú, retiro, consignación, saldo, comprobante e historial.
 */
public class VentanaCajero extends JFrame {

    private static final String LOGIN = "login";
    private static final String REGISTRO = "registro";
    private static final String MENU = "menu";
    private static final String RETIRO = "retiro";
    private static final String CONSIGNACION = "consignacion";
    private static final String SALDO = "saldo";
    private static final String COMPROBANTE = "comprobante";
    private static final String HISTORIAL = "historial";

    private final CajeroService cajero;
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);

    // Login
    private final JTextField txtTarjeta = new JTextField();
    private final JPasswordField txtPin = new JPasswordField();
    // Registro
    private final JTextField txtRegNombre = new JTextField();
    private final JPasswordField txtRegPin = new JPasswordField();
    private final JPasswordField txtRegPin2 = new JPasswordField();
    private final JTextField txtRegSaldo = new JTextField();
    // Menú
    private final JLabel lblBienvenida = Estilo.titulo("");
    private final JLabel lblTarjetaMenu = Estilo.subtitulo("");
    // Retiro / consignación
    private final JTextField txtRetiroOtro = new JTextField();
    private final JTextField txtConsignacion = new JTextField();
    // Saldo
    private final JLabel lblSaldo = new JLabel("", SwingConstants.CENTER);
    // Comprobante
    private final JLabel lblCompTitulo = Estilo.titulo("");
    private final JTextArea txtComprobante = new JTextArea();
    // Historial
    private final DefaultTableModel modeloHistorial =
            new DefaultTableModel(new Object[]{"Fecha", "Operación", "Valor", "Saldo"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

    public VentanaCajero(CajeroService cajero) {
        super("Cajero Automático - UMB Bank");
        this.cajero = cajero;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(720, 560);
        setMinimumSize(new Dimension(640, 520));
        setLocationRelativeTo(null);

        JPanel marco = new JPanel(new BorderLayout());
        marco.setBackground(Estilo.FONDO);
        marco.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel encabezado = new JLabel("UMB Bank  ·  Cajero Automático", SwingConstants.CENTER);
        encabezado.setFont(new Font("SansSerif", Font.BOLD, 18));
        encabezado.setForeground(Color.WHITE);
        encabezado.setBorder(new EmptyBorder(0, 0, 12, 0));
        marco.add(encabezado, BorderLayout.NORTH);

        contenedor.setBorder(new LineBorder(new Color(0x90A4AE), 3, true));
        contenedor.add(crearLogin(), LOGIN);
        contenedor.add(crearRegistro(), REGISTRO);
        contenedor.add(crearMenu(), MENU);
        contenedor.add(crearRetiro(), RETIRO);
        contenedor.add(crearConsignacion(), CONSIGNACION);
        contenedor.add(crearSaldo(), SALDO);
        contenedor.add(crearComprobante(), COMPROBANTE);
        contenedor.add(crearHistorial(), HISTORIAL);
        marco.add(contenedor, BorderLayout.CENTER);

        setContentPane(marco);
        mostrar(LOGIN);
    }

    // ================================================================ Pantallas

    private JPanel crearLogin() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();

        p.add(Estilo.titulo("Bienvenido"), g);
        g.gridy++;
        p.add(Estilo.subtitulo("Ingrese su tarjeta y su clave personal (PIN)"), g);

        g.gridy++; g.insets = new Insets(20, 0, 4, 0);
        p.add(etiqueta("Número de tarjeta (16 dígitos)"), g);
        g.gridy++; g.insets = new Insets(0, 0, 8, 0);
        configurarCampo(txtTarjeta);
        p.add(txtTarjeta, g);

        g.gridy++; g.insets = new Insets(8, 0, 4, 0);
        p.add(etiqueta("PIN (4 dígitos)"), g);
        g.gridy++; g.insets = new Insets(0, 0, 8, 0);
        configurarCampo(txtPin);
        p.add(txtPin, g);

        JButton ingresar = Estilo.boton("Ingresar", Estilo.PRIMARIO);
        ingresar.addActionListener(e -> ingresar());
        txtPin.addActionListener(e -> ingresar());
        txtTarjeta.addActionListener(e -> txtPin.requestFocusInWindow());
        g.gridy++; g.insets = new Insets(16, 0, 8, 0);
        p.add(ingresar, g);

        JButton nueva = Estilo.boton("¿No tiene cuenta? Crear cuenta nueva", Estilo.EXITO);
        nueva.addActionListener(e -> abrirRegistro());
        g.gridy++; g.insets = new Insets(0, 0, 8, 0);
        p.add(nueva, g);

        JLabel ayuda = Estilo.subtitulo("<html><center>Cuenta de prueba: 4532 0151 1283 0366 · PIN 1234</center></html>");
        ayuda.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g.gridy++;
        p.add(ayuda, g);
        return p;
    }

    private JPanel crearRegistro() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();

        p.add(Estilo.titulo("Crear cuenta nueva"), g);
        g.gridy++;
        p.add(Estilo.subtitulo("El sistema le asignará un número de tarjeta"), g);

        JPanel form = new JPanel(new GridLayout(4, 2, 12, 10));
        form.setOpaque(false);
        for (JTextField c : new JTextField[]{txtRegNombre, txtRegPin, txtRegPin2, txtRegSaldo}) {
            configurarCampo(c);
            c.setFont(new Font("SansSerif", Font.BOLD, 16));
        }
        form.add(etiqueta("Nombre completo del titular"));
        form.add(txtRegNombre);
        form.add(etiqueta("PIN (4 dígitos)"));
        form.add(txtRegPin);
        form.add(etiqueta("Confirmar PIN"));
        form.add(txtRegPin2);
        form.add(etiqueta("Consignación inicial (opcional)"));
        form.add(txtRegSaldo);
        g.gridy++; g.insets = new Insets(20, 0, 8, 0);
        p.add(form, g);

        JPanel botones = new JPanel(new GridLayout(1, 2, 12, 0));
        botones.setOpaque(false);
        JButton crear = Estilo.boton("Crear cuenta", Estilo.EXITO);
        JButton cancelar = Estilo.boton("Cancelar", new Color(0x546E7A));
        crear.addActionListener(e -> crearCuenta());
        txtRegSaldo.addActionListener(e -> crearCuenta());
        cancelar.addActionListener(e -> mostrar(LOGIN));
        botones.add(cancelar);
        botones.add(crear);
        g.gridy++; g.insets = new Insets(16, 0, 4, 0);
        p.add(botones, g);
        return p;
    }

    private JPanel crearMenu() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new BorderLayout(0, 20));

        JPanel cab = new JPanel(new GridLayout(2, 1));
        cab.setOpaque(false);
        cab.add(lblBienvenida);
        cab.add(lblTarjetaMenu);
        p.add(cab, BorderLayout.NORTH);

        JPanel botones = new JPanel(new GridLayout(3, 2, 16, 16));
        botones.setOpaque(false);
        JButton bRetiro = Estilo.boton("Retirar dinero", Estilo.PRIMARIO);
        JButton bConsig = Estilo.boton("Consignar", Estilo.PRIMARIO);
        JButton bSaldo = Estilo.boton("Consultar saldo", Estilo.PRIMARIO);
        JButton bHist = Estilo.boton("Últimos movimientos", Estilo.PRIMARIO);
        JButton bSalir = Estilo.boton("Salir / Retirar tarjeta", Estilo.PELIGRO);

        bRetiro.addActionListener(e -> { txtRetiroOtro.setText(""); mostrar(RETIRO); });
        bConsig.addActionListener(e -> { txtConsignacion.setText(""); mostrar(CONSIGNACION); txtConsignacion.requestFocusInWindow(); });
        bSaldo.addActionListener(e -> consultarSaldo());
        bHist.addActionListener(e -> verHistorial());
        bSalir.addActionListener(e -> salir());

        botones.add(bRetiro);
        botones.add(bConsig);
        botones.add(bSaldo);
        botones.add(bHist);
        botones.add(new JLabel());
        botones.add(bSalir);
        p.add(botones, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearRetiro() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new BorderLayout(0, 16));

        JPanel cab = new JPanel(new GridLayout(2, 1));
        cab.setOpaque(false);
        cab.add(Estilo.titulo("Retiro"));
        cab.add(Estilo.subtitulo("Seleccione un monto o escriba otro valor (múltiplo de $10.000)"));
        p.add(cab, BorderLayout.NORTH);

        JPanel rapidos = new JPanel(new GridLayout(2, 3, 12, 12));
        rapidos.setOpaque(false);
        int[] montos = {20_000, 50_000, 100_000, 200_000, 300_000, 500_000};
        for (int m : montos) {
            JButton b = Estilo.boton(formatoPesos(m), Estilo.PRIMARIO_OSCURO);
            b.addActionListener(e -> retirar(m));
            rapidos.add(b);
        }

        JPanel otro = new JPanel(new BorderLayout(10, 0));
        otro.setOpaque(false);
        otro.setBorder(new EmptyBorder(16, 0, 0, 0));
        configurarCampo(txtRetiroOtro);
        JButton bOtro = Estilo.boton("Retirar otro valor", Estilo.EXITO);
        bOtro.addActionListener(e -> retirarOtro());
        txtRetiroOtro.addActionListener(e -> retirarOtro());
        otro.add(etiqueta("Otro valor: "), BorderLayout.WEST);
        otro.add(txtRetiroOtro, BorderLayout.CENTER);
        otro.add(bOtro, BorderLayout.EAST);

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(rapidos, BorderLayout.CENTER);
        centro.add(otro, BorderLayout.SOUTH);
        p.add(centro, BorderLayout.CENTER);
        p.add(botonVolver(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearConsignacion() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();

        p.add(Estilo.titulo("Consignación"), g);
        g.gridy++;
        p.add(Estilo.subtitulo("Ingrese el valor a consignar en su cuenta"), g);
        g.gridy++; g.insets = new Insets(24, 0, 4, 0);
        p.add(etiqueta("Valor (mínimo $1.000, máximo $10.000.000)"), g);
        g.gridy++; g.insets = new Insets(0, 0, 8, 0);
        configurarCampo(txtConsignacion);
        p.add(txtConsignacion, g);

        JButton bOk = Estilo.boton("Consignar", Estilo.EXITO);
        bOk.addActionListener(e -> consignar());
        txtConsignacion.addActionListener(e -> consignar());
        g.gridy++; g.insets = new Insets(16, 0, 8, 0);
        p.add(bOk, g);
        g.gridy++;
        p.add(botonVolver(), g);
        return p;
    }

    private JPanel crearSaldo() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new GridBagLayout());
        GridBagConstraints g = base();
        p.add(Estilo.titulo("Saldo disponible"), g);
        g.gridy++; g.insets = new Insets(24, 0, 24, 0);
        lblSaldo.setFont(Estilo.SALDO);
        lblSaldo.setForeground(Estilo.EXITO);
        p.add(lblSaldo, g);
        g.gridy++; g.insets = new Insets(8, 0, 8, 0);
        p.add(botonVolver(), g);
        return p;
    }

    private JPanel crearComprobante() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new BorderLayout(0, 16));
        lblCompTitulo.setForeground(Estilo.EXITO);
        p.add(lblCompTitulo, BorderLayout.NORTH);

        txtComprobante.setEditable(false);
        txtComprobante.setFont(new Font("Monospaced", Font.PLAIN, 15));
        txtComprobante.setBackground(Color.WHITE);
        txtComprobante.setBorder(new EmptyBorder(16, 20, 16, 20));
        JScrollPane sp = new JScrollPane(txtComprobante);
        sp.setBorder(new LineBorder(new Color(0xCFD8DC)));
        p.add(sp, BorderLayout.CENTER);
        p.add(botonVolver(), BorderLayout.SOUTH);
        return p;
    }

    private JPanel crearHistorial() {
        JPanel p = Estilo.panelPantalla();
        p.setLayout(new BorderLayout(0, 16));
        p.add(Estilo.titulo("Últimos movimientos"), BorderLayout.NORTH);
        JTable tabla = new JTable(modeloHistorial);
        tabla.setRowHeight(26);
        tabla.setFont(new Font("SansSerif", Font.PLAIN, 13));
        tabla.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        p.add(botonVolver(), BorderLayout.SOUTH);
        return p;
    }

    // ================================================================ Acciones

    private void ingresar() {
        try {
            Cuenta c = cajero.iniciarSesion(txtTarjeta.getText(), new String(txtPin.getPassword()));
            lblBienvenida.setText("Hola, " + c.getTitular());
            lblTarjetaMenu.setText("Tarjeta " + c.getTarjetaEnmascarada() + "  ·  ¿Qué desea hacer?");
            txtPin.setText("");
            mostrar(MENU);
        } catch (OperacionException ex) {
            txtPin.setText("");
            error(ex.getMessage());
        }
    }

    private void abrirRegistro() {
        for (JTextField c : new JTextField[]{txtRegNombre, txtRegPin, txtRegPin2, txtRegSaldo}) c.setText("");
        mostrar(REGISTRO);
        txtRegNombre.requestFocusInWindow();
    }

    private void crearCuenta() {
        try {
            String textoSaldo = txtRegSaldo.getText().trim();
            double saldo = textoSaldo.isEmpty() ? 0 : CajeroService.leerMonto(textoSaldo);
            Cuenta c = cajero.crearCuenta(txtRegNombre.getText(), new String(txtRegPin.getPassword()),
                    new String(txtRegPin2.getPassword()), saldo);
            String tarjeta = CajeroService.formatoTarjeta(c.getNumeroTarjeta());
            JOptionPane.showMessageDialog(this,
                    "<html><b>¡Cuenta creada con éxito!</b><br><br>Titular: " + c.getTitular()
                            + "<br>Número de tarjeta: <b>" + tarjeta + "</b><br>Saldo inicial: "
                            + formatoPesos(c.getSaldo()) + "<br><br>Anote su número de tarjeta: lo necesitará para ingresar.</html>",
                    "Cuenta creada", JOptionPane.INFORMATION_MESSAGE);
            txtTarjeta.setText(tarjeta);
            txtPin.setText("");
            mostrar(LOGIN);
            txtPin.requestFocusInWindow();
        } catch (OperacionException ex) {
            error(ex.getMessage());
        }
    }

    private void retirarOtro() {
        try {
            retirar(CajeroService.leerMonto(txtRetiroOtro.getText()));
        } catch (OperacionException ex) {
            error(ex.getMessage());
        }
    }

    private void retirar(double monto) {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Confirma el retiro de " + formatoPesos(monto) + "?",
                "Confirmar retiro", JOptionPane.YES_NO_OPTION);
        if (r != JOptionPane.YES_OPTION) return;
        try {
            double saldo = cajero.retirar(monto);
            comprobante("Retiro exitoso", TipoMovimiento.RETIRO, monto, saldo);
        } catch (OperacionException ex) {
            error(ex.getMessage());
        }
    }

    private void consignar() {
        try {
            double monto = CajeroService.leerMonto(txtConsignacion.getText());
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Confirma la consignación de " + formatoPesos(monto) + "?",
                    "Confirmar consignación", JOptionPane.YES_NO_OPTION);
            if (r != JOptionPane.YES_OPTION) return;
            double saldo = cajero.consignar(monto);
            comprobante("Consignación exitosa", TipoMovimiento.CONSIGNACION, monto, saldo);
        } catch (OperacionException ex) {
            error(ex.getMessage());
        }
    }

    private void consultarSaldo() {
        try {
            lblSaldo.setText(formatoPesos(cajero.consultarSaldo()));
            mostrar(SALDO);
        } catch (OperacionException ex) {
            error(ex.getMessage());
        }
    }

    private void verHistorial() {
        modeloHistorial.setRowCount(0);
        List<Movimiento> movs = cajero.getSesionActual().getMovimientos();
        for (int i = movs.size() - 1; i >= 0 && i >= movs.size() - 10; i--) {
            Movimiento m = movs.get(i);
            modeloHistorial.addRow(new Object[]{
                    m.getFecha().format(Movimiento.FORMATO_FECHA),
                    m.getTipo().getDescripcion(),
                    m.getTipo() == TipoMovimiento.CONSULTA ? "-" : formatoPesos(m.getValor()),
                    formatoPesos(m.getSaldoResultante())});
        }
        mostrar(HISTORIAL);
    }

    private void salir() {
        cajero.cerrarSesion();
        txtTarjeta.setText("");
        txtPin.setText("");
        JOptionPane.showMessageDialog(this, "Gracias por usar nuestro cajero. No olvide retirar su tarjeta.",
                "Sesión finalizada", JOptionPane.INFORMATION_MESSAGE);
        mostrar(LOGIN);
    }

    // ================================================================ Utilidades

    private void comprobante(String titulo, TipoMovimiento tipo, double monto, double saldo) {
        Cuenta c = cajero.getSesionActual();
        Movimiento ultimo = c.getMovimientos().get(c.getMovimientos().size() - 1);
        lblCompTitulo.setText(titulo);
        txtComprobante.setText(
                "        COMPROBANTE DE TRANSACCIÓN\n"
              + "   ----------------------------------\n"
              + "   Fecha:      " + ultimo.getFecha().format(Movimiento.FORMATO_FECHA) + "\n"
              + "   Tarjeta:    " + c.getTarjetaEnmascarada() + "\n"
              + "   Titular:    " + c.getTitular() + "\n"
              + "   Operación:  " + tipo.getDescripcion() + "\n"
              + "   Valor:      " + formatoPesos(monto) + "\n"
              + "   Saldo:      " + formatoPesos(saldo) + "\n"
              + "   ----------------------------------\n"
              + "        ¡Gracias por su transacción!");
        mostrar(COMPROBANTE);
    }

    private void mostrar(String pantalla) {
        tarjetas.show(contenedor, pantalla);
        if (LOGIN.equals(pantalla)) txtTarjeta.requestFocusInWindow();
    }

    private void error(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Operación no realizada", JOptionPane.ERROR_MESSAGE);
    }

    private JButton botonVolver() {
        JButton b = Estilo.boton("Volver al menú", new Color(0x546E7A));
        b.addActionListener(e -> mostrar(MENU));
        return b;
    }

    private static JLabel etiqueta(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("SansSerif", Font.BOLD, 13));
        l.setForeground(Estilo.TEXTO);
        return l;
    }

    private static void configurarCampo(JTextField campo) {
        campo.setFont(Estilo.CAMPO);
        campo.setHorizontalAlignment(SwingConstants.CENTER);
        campo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0x90A4AE), 1, true), new EmptyBorder(8, 10, 8, 10)));
    }

    private static GridBagConstraints base() {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;
        g.insets = new Insets(4, 0, 4, 0);
        return g;
    }
}
