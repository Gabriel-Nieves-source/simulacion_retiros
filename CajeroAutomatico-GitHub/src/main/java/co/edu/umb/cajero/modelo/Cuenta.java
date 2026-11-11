package co.edu.umb.cajero.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Cuenta bancaria asociada a una tarjeta. Encapsula el saldo y el historial
 * de movimientos; solo el servicio del cajero modifica el saldo.
 */
public class Cuenta {

    private final String numeroTarjeta;
    private final String titular;
    private final String pin;
    private double saldo;
    private boolean bloqueada;
    private final List<Movimiento> movimientos = new ArrayList<>();

    public Cuenta(String numeroTarjeta, String titular, String pin, double saldo, boolean bloqueada) {
        this.numeroTarjeta = numeroTarjeta;
        this.titular = titular;
        this.pin = pin;
        this.saldo = saldo;
        this.bloqueada = bloqueada;
    }

    public String getNumeroTarjeta() { return numeroTarjeta; }
    public String getTitular() { return titular; }
    public double getSaldo() { return saldo; }
    public boolean isBloqueada() { return bloqueada; }

    public boolean validarPin(String pinIngresado) {
        return pin.equals(pinIngresado);
    }

    /** PIN visible solo para la capa de persistencia. */
    String getPin() { return pin; }

    void setSaldo(double saldo) { this.saldo = saldo; }

    public void bloquear() { this.bloqueada = true; }

    public void agregarMovimiento(Movimiento m) { movimientos.add(m); }

    public List<Movimiento> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    /** Número de tarjeta enmascarado para mostrar en pantalla: **** **** **** 1234 */
    public String getTarjetaEnmascarada() {
        String ultimos = numeroTarjeta.substring(numeroTarjeta.length() - 4);
        return "**** **** **** " + ultimos;
    }

    public String aLineaArchivo() {
        return numeroTarjeta + ";" + titular + ";" + pin + ";" + saldo + ";" + bloqueada;
    }

    public static Cuenta desdeLineaArchivo(String linea) {
        String[] p = linea.split(";");
        return new Cuenta(p[0], p[1], p[2], Double.parseDouble(p[3]), Boolean.parseBoolean(p[4]));
    }

    /** Acceso controlado al saldo para el paquete de servicio. */
    public static void actualizarSaldo(Cuenta cuenta, double nuevoSaldo) {
        cuenta.setSaldo(nuevoSaldo);
    }
}
