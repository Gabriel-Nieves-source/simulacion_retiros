package co.edu.umb.cajero.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una operación realizada sobre una cuenta (retiro, consignación o consulta).
 */
public class Movimiento {

    public static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime fecha;
    private final TipoMovimiento tipo;
    private final double valor;
    private final double saldoResultante;

    public Movimiento(LocalDateTime fecha, TipoMovimiento tipo, double valor, double saldoResultante) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.valor = valor;
        this.saldoResultante = saldoResultante;
    }

    public LocalDateTime getFecha() { return fecha; }
    public TipoMovimiento getTipo() { return tipo; }
    public double getValor() { return valor; }
    public double getSaldoResultante() { return saldoResultante; }

    /** Convierte el movimiento a una línea de texto para guardarlo en archivo. */
    public String aLineaArchivo() {
        return fecha.format(FORMATO_FECHA) + ";" + tipo.name() + ";" + valor + ";" + saldoResultante;
    }

    /** Reconstruye un movimiento a partir de una línea guardada. */
    public static Movimiento desdeLineaArchivo(String linea) {
        String[] p = linea.split(";");
        return new Movimiento(
                LocalDateTime.parse(p[0], FORMATO_FECHA),
                TipoMovimiento.valueOf(p[1]),
                Double.parseDouble(p[2]),
                Double.parseDouble(p[3]));
    }
}
