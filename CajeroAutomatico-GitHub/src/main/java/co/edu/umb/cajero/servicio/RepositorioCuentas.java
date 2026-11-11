package co.edu.umb.cajero.servicio;

import co.edu.umb.cajero.modelo.Cuenta;
import co.edu.umb.cajero.modelo.Movimiento;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistencia de las cuentas en archivos de texto plano dentro de una carpeta "datos".
 *  - cuentas.txt                 : una cuenta por línea
 *  - movimientos_TARJETA.txt     : historial de cada cuenta
 * Si la carpeta no existe se crean cuentas de prueba para poder usar el cajero de inmediato.
 */
public class RepositorioCuentas {

    private final Path carpeta;
    private final Path archivoCuentas;
    private final Map<String, Cuenta> cuentas = new LinkedHashMap<>();

    public RepositorioCuentas(Path carpeta) {
        this.carpeta = carpeta;
        this.archivoCuentas = carpeta.resolve("cuentas.txt");
        cargar();
    }

    public Cuenta buscar(String numeroTarjeta) {
        return cuentas.get(numeroTarjeta);
    }

    public boolean existe(String numeroTarjeta) {
        return cuentas.containsKey(numeroTarjeta);
    }

    /** Agrega una cuenta nueva y la guarda de inmediato en el archivo. */
    public void registrar(Cuenta cuenta) {
        cuentas.put(cuenta.getNumeroTarjeta(), cuenta);
        guardar();
    }

    public List<Cuenta> listar() {
        return new ArrayList<>(cuentas.values());
    }

    private void cargar() {
        try {
            if (!Files.exists(archivoCuentas)) {
                crearCuentasDePrueba();
                guardar();
                return;
            }
            for (String linea : Files.readAllLines(archivoCuentas, StandardCharsets.UTF_8)) {
                if (linea.isBlank()) continue;
                Cuenta c = Cuenta.desdeLineaArchivo(linea);
                Path archivoMov = archivoMovimientos(c.getNumeroTarjeta());
                if (Files.exists(archivoMov)) {
                    for (String l : Files.readAllLines(archivoMov, StandardCharsets.UTF_8)) {
                        if (!l.isBlank()) c.agregarMovimiento(Movimiento.desdeLineaArchivo(l));
                    }
                }
                cuentas.put(c.getNumeroTarjeta(), c);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("No se pudieron leer los datos del cajero: " + e.getMessage(), e);
        }
    }

    /** Guarda todas las cuentas y sus movimientos. Se llama después de cada operación. */
    public void guardar() {
        try {
            Files.createDirectories(carpeta);
            List<String> lineas = new ArrayList<>();
            for (Cuenta c : cuentas.values()) {
                lineas.add(c.aLineaArchivo());
                List<String> movs = new ArrayList<>();
                for (Movimiento m : c.getMovimientos()) movs.add(m.aLineaArchivo());
                Files.write(archivoMovimientos(c.getNumeroTarjeta()), movs, StandardCharsets.UTF_8);
            }
            Files.write(archivoCuentas, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron guardar los datos del cajero: " + e.getMessage(), e);
        }
    }

    private Path archivoMovimientos(String tarjeta) {
        return carpeta.resolve("movimientos_" + tarjeta + ".txt");
    }

    private void crearCuentasDePrueba() {
        agregar(new Cuenta("4532015112830366", "Ana María Pérez", "1234", 1_500_000, false));
        agregar(new Cuenta("5425233430109903", "Carlos Rodríguez", "4321", 350_000, false));
        agregar(new Cuenta("4916338506082832", "Laura Gómez", "1111", 5_000_000, false));
    }

    private void agregar(Cuenta c) {
        cuentas.put(c.getNumeroTarjeta(), c);
    }
}
