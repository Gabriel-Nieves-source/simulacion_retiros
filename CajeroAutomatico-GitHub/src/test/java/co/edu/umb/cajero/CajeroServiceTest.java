package co.edu.umb.cajero;

import co.edu.umb.cajero.servicio.CajeroService;
import co.edu.umb.cajero.servicio.OperacionException;
import co.edu.umb.cajero.servicio.RepositorioCuentas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias de la lógica del cajero. Cada prueba usa una carpeta temporal
 * con las cuentas de prueba (Ana María: saldo $1.500.000, PIN 1234).
 */
class CajeroServiceTest {

    private static final String TARJETA = "4532015112830366";
    private static final String PIN = "1234";

    @TempDir
    Path carpeta;
    private CajeroService cajero;

    @BeforeEach
    void preparar() {
        cajero = new CajeroService(new RepositorioCuentas(carpeta));
    }

    @Test
    void inicioDeSesionCorrecto() throws OperacionException {
        assertEquals("Ana María Pérez", cajero.iniciarSesion(TARJETA, PIN).getTitular());
    }

    @Test
    void aceptaTarjetaConEspacios() throws OperacionException {
        assertNotNull(cajero.iniciarSesion("4532 0151 1283 0366", PIN));
    }

    @Test
    void pinIncorrectoInformaIntentosRestantes() {
        OperacionException e = assertThrows(OperacionException.class, () -> cajero.iniciarSesion(TARJETA, "0000"));
        assertTrue(e.getMessage().contains("2 intento"));
    }

    @Test
    void tresIntentosFallidosBloqueanLaTarjeta() {
        for (int i = 0; i < 3; i++) {
            assertThrows(OperacionException.class, () -> cajero.iniciarSesion(TARJETA, "0000"));
        }
        OperacionException e = assertThrows(OperacionException.class, () -> cajero.iniciarSesion(TARJETA, PIN));
        assertTrue(e.getMessage().contains("bloqueada"));
    }

    @Test
    void consultaDeSaldo() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        assertEquals(1_500_000, cajero.consultarSaldo());
    }

    @Test
    void retiroDescuentaDelSaldo() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        assertEquals(1_400_000, cajero.retirar(100_000));
    }

    @Test
    void retiroConSaldoInsuficienteSeRechaza() throws OperacionException {
        cajero.iniciarSesion("5425233430109903", "4321"); // saldo 350.000
        assertThrows(OperacionException.class, () -> cajero.retirar(400_000));
        assertEquals(350_000, cajero.consultarSaldo());
    }

    @Test
    void retiroQueNoEsMultiploDe10000SeRechaza() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        assertThrows(OperacionException.class, () -> cajero.retirar(15_000));
    }

    @Test
    void retiroMayorAlLimiteSeRechaza() throws OperacionException {
        cajero.iniciarSesion("4916338506082832", "1111"); // saldo 5.000.000
        assertThrows(OperacionException.class, () -> cajero.retirar(2_010_000));
    }

    @Test
    void consignacionAumentaElSaldo() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        assertEquals(1_750_000, cajero.consignar(250_000));
    }

    @Test
    void consignacionMenorAlMinimoSeRechaza() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        assertThrows(OperacionException.class, () -> cajero.consignar(500));
    }

    @Test
    void operacionSinSesionSeRechaza() {
        assertThrows(OperacionException.class, () -> cajero.retirar(10_000));
    }

    @Test
    void losDatosSeGuardanEnArchivo() throws OperacionException {
        cajero.iniciarSesion(TARJETA, PIN);
        cajero.retirar(200_000);
        CajeroService otroCajero = new CajeroService(new RepositorioCuentas(carpeta));
        otroCajero.iniciarSesion(TARJETA, PIN);
        assertEquals(1_300_000, otroCajero.consultarSaldo());
        // 1 retiro + 1 consulta de saldo
        assertEquals(2, otroCajero.getSesionActual().getMovimientos().size());
    }

    @Test
    void usuarioNuevoSeRegistraEIngresa() throws OperacionException {
        var nueva = cajero.crearCuenta("Pedro Martínez", "2580", "2580", 100_000);
        assertTrue(nueva.getNumeroTarjeta().matches("\\d{16}"));
        cajero.iniciarSesion(nueva.getNumeroTarjeta(), "2580");
        assertEquals(60_000, cajero.retirar(40_000));
        assertEquals(560_000, cajero.consignar(500_000));
    }

    @Test
    void cuentaNuevaSeConservaAlReabrir() throws OperacionException {
        String tarjeta = cajero.crearCuenta("Pedro Martínez", "2580", "2580", 0).getNumeroTarjeta();
        CajeroService otroCajero = new CajeroService(new RepositorioCuentas(carpeta));
        otroCajero.iniciarSesion(tarjeta, "2580");
        assertEquals(0, otroCajero.consultarSaldo());
    }

    @Test
    void registroConPinsDistintosSeRechaza() {
        assertThrows(OperacionException.class, () -> cajero.crearCuenta("Pedro Martínez", "2580", "2581", 0));
    }

    @Test
    void registroConPinFacilSeRechaza() {
        assertThrows(OperacionException.class, () -> cajero.crearCuenta("Pedro Martínez", "7777", "7777", 0));
    }

    @Test
    void registroConNombreInvalidoSeRechaza() {
        assertThrows(OperacionException.class, () -> cajero.crearCuenta("P3dro", "2580", "2580", 0));
    }

    @Test
    void tarjetaGeneradaCumpleLuhn() {
        for (int i = 0; i < 50; i++) {
            String t = CajeroService.generarNumeroTarjeta();
            int suma = 0;
            for (int k = 0; k < 16; k++) {
                int d = t.charAt(15 - k) - '0';
                if (k % 2 == 1) { d *= 2; if (d > 9) d -= 9; }
                suma += d;
            }
            assertEquals(0, suma % 10);
        }
    }

    @Test
    void leerMontoAceptaPuntosYSigno() throws OperacionException {
        assertEquals(150_000, CajeroService.leerMonto("$150.000"));
        assertThrows(OperacionException.class, () -> CajeroService.leerMonto("abc"));
    }
}
