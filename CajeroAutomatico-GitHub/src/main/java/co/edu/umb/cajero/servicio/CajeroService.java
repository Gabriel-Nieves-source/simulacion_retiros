package co.edu.umb.cajero.servicio;

import co.edu.umb.cajero.modelo.Cuenta;
import co.edu.umb.cajero.modelo.Movimiento;
import co.edu.umb.cajero.modelo.TipoMovimiento;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Lógica de negocio del cajero automático: autenticación, retiros, consignaciones
 * y consultas de saldo. La interfaz gráfica solo llama a estos métodos.
 */
public class CajeroService {

    public static final int MAX_INTENTOS_PIN = 3;
    public static final double MULTIPLO_RETIRO = 10_000;
    public static final double MAX_RETIRO_TRANSACCION = 2_000_000;
    public static final double MIN_CONSIGNACION = 1_000;
    public static final double MAX_CONSIGNACION = 10_000_000;

    private static final java.security.SecureRandom ALEATORIO = new java.security.SecureRandom();

    private final RepositorioCuentas repositorio;
    private final Map<String, Integer> intentosFallidos = new HashMap<>();
    private Cuenta sesionActual;

    public CajeroService(RepositorioCuentas repositorio) {
        this.repositorio = repositorio;
    }

    // ---------------------------------------------------------------- Sesión

    public Cuenta iniciarSesion(String numeroTarjeta, String pin) throws OperacionException {
        String tarjeta = numeroTarjeta == null ? "" : numeroTarjeta.replaceAll("\\s", "");
        if (!tarjeta.matches("\\d{16}")) {
            throw new OperacionException("El número de tarjeta debe tener 16 dígitos.");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new OperacionException("El PIN debe tener 4 dígitos.");
        }
        Cuenta cuenta = repositorio.buscar(tarjeta);
        if (cuenta == null) {
            throw new OperacionException("Tarjeta no registrada en el sistema.");
        }
        if (cuenta.isBloqueada()) {
            throw new OperacionException("Tarjeta bloqueada. Comuníquese con su banco.");
        }
        if (!cuenta.validarPin(pin)) {
            int intentos = intentosFallidos.merge(tarjeta, 1, Integer::sum);
            if (intentos >= MAX_INTENTOS_PIN) {
                cuenta.bloquear();
                repositorio.guardar();
                throw new OperacionException("PIN incorrecto. Superó los " + MAX_INTENTOS_PIN
                        + " intentos: la tarjeta ha sido bloqueada.");
            }
            int restantes = MAX_INTENTOS_PIN - intentos;
            throw new OperacionException("PIN incorrecto. Le quedan " + restantes + " intento(s).");
        }
        intentosFallidos.remove(tarjeta);
        sesionActual = cuenta;
        return cuenta;
    }

    /**
     * Registra un cliente nuevo. El sistema genera el número de tarjeta (16 dígitos,
     * válido según el algoritmo de Luhn) y, si se indica, hace una consignación inicial.
     */
    public Cuenta crearCuenta(String titular, String pin, String confirmacionPin, double saldoInicial)
            throws OperacionException {
        String nombre = titular == null ? "" : titular.trim().replaceAll("\\s+", " ");
        if (nombre.length() < 3) {
            throw new OperacionException("Ingrese el nombre completo del titular.");
        }
        if (!nombre.matches("[\\p{L} ]+")) {
            throw new OperacionException("El nombre solo puede contener letras y espacios.");
        }
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new OperacionException("El PIN debe tener exactamente 4 dígitos.");
        }
        if (pin.chars().distinct().count() == 1 || "1234".equals(pin) || "4321".equals(pin)) {
            throw new OperacionException("El PIN es muy fácil de adivinar. Elija otro.");
        }
        if (!pin.equals(confirmacionPin)) {
            throw new OperacionException("La confirmación no coincide con el PIN.");
        }
        if (saldoInicial != 0 && (saldoInicial < MIN_CONSIGNACION || saldoInicial > MAX_CONSIGNACION)) {
            throw new OperacionException("La consignación inicial debe ser $0 o estar entre "
                    + formatoPesos(MIN_CONSIGNACION) + " y " + formatoPesos(MAX_CONSIGNACION) + ".");
        }

        String tarjeta;
        do {
            tarjeta = generarNumeroTarjeta();
        } while (repositorio.existe(tarjeta));

        Cuenta nueva = new Cuenta(tarjeta, nombre, pin, saldoInicial, false);
        if (saldoInicial > 0) {
            nueva.agregarMovimiento(new Movimiento(LocalDateTime.now(), TipoMovimiento.CONSIGNACION,
                    saldoInicial, saldoInicial));
        }
        repositorio.registrar(nueva);
        return nueva;
    }

    /** Genera 16 dígitos que empiezan por 4 y cuyo último dígito cumple el algoritmo de Luhn. */
    public static String generarNumeroTarjeta() {
        StringBuilder sb = new StringBuilder("4");
        for (int i = 0; i < 14; i++) sb.append(ALEATORIO.nextInt(10));
        return sb.append(digitoLuhn(sb.toString())).toString();
    }

    static int digitoLuhn(String sinVerificador) {
        int suma = 0;
        boolean doblar = true;
        for (int i = sinVerificador.length() - 1; i >= 0; i--) {
            int d = sinVerificador.charAt(i) - '0';
            if (doblar) { d *= 2; if (d > 9) d -= 9; }
            suma += d;
            doblar = !doblar;
        }
        return (10 - suma % 10) % 10;
    }

    public static String formatoTarjeta(String tarjeta) {
        return tarjeta.replaceAll("(\\d{4})(?=\\d)", "$1 ");
    }

    public void cerrarSesion() {
        sesionActual = null;
    }

    public Cuenta getSesionActual() {
        return sesionActual;
    }

    // ------------------------------------------------------------ Operaciones

    public double consultarSaldo() throws OperacionException {
        Cuenta c = exigirSesion();
        registrar(c, TipoMovimiento.CONSULTA, 0);
        return c.getSaldo();
    }

    public double retirar(double monto) throws OperacionException {
        Cuenta c = exigirSesion();
        if (monto <= 0) {
            throw new OperacionException("El monto a retirar debe ser mayor a cero.");
        }
        if (monto % MULTIPLO_RETIRO != 0) {
            throw new OperacionException("El cajero solo entrega billetes: el monto debe ser múltiplo de "
                    + formatoPesos(MULTIPLO_RETIRO) + ".");
        }
        if (monto > MAX_RETIRO_TRANSACCION) {
            throw new OperacionException("El retiro máximo por transacción es "
                    + formatoPesos(MAX_RETIRO_TRANSACCION) + ".");
        }
        if (monto > c.getSaldo()) {
            throw new OperacionException("Saldo insuficiente. Su saldo disponible es "
                    + formatoPesos(c.getSaldo()) + ".");
        }
        Cuenta.actualizarSaldo(c, c.getSaldo() - monto);
        registrar(c, TipoMovimiento.RETIRO, monto);
        return c.getSaldo();
    }

    public double consignar(double monto) throws OperacionException {
        Cuenta c = exigirSesion();
        if (monto < MIN_CONSIGNACION) {
            throw new OperacionException("La consignación mínima es " + formatoPesos(MIN_CONSIGNACION) + ".");
        }
        if (monto > MAX_CONSIGNACION) {
            throw new OperacionException("La consignación máxima por transacción es "
                    + formatoPesos(MAX_CONSIGNACION) + ".");
        }
        if (monto != Math.floor(monto)) {
            throw new OperacionException("El monto no puede tener centavos.");
        }
        Cuenta.actualizarSaldo(c, c.getSaldo() + monto);
        registrar(c, TipoMovimiento.CONSIGNACION, monto);
        return c.getSaldo();
    }

    /** Convierte el texto escrito por el usuario (con o sin puntos) a número. */
    public static double leerMonto(String texto) throws OperacionException {
        if (texto == null || texto.isBlank()) {
            throw new OperacionException("Ingrese un monto.");
        }
        String limpio = texto.replace("$", "").replace(".", "").replace(",", "").trim();
        if (!limpio.matches("\\d{1,12}")) {
            throw new OperacionException("El monto solo puede contener números.");
        }
        return Double.parseDouble(limpio);
    }

    public static String formatoPesos(double valor) {
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CO"));
        return "$" + nf.format(valor);
    }

    // ------------------------------------------------------------- Internos

    private Cuenta exigirSesion() throws OperacionException {
        if (sesionActual == null) {
            throw new OperacionException("Debe iniciar sesión primero.");
        }
        return sesionActual;
    }

    private void registrar(Cuenta c, TipoMovimiento tipo, double valor) {
        c.agregarMovimiento(new Movimiento(LocalDateTime.now(), tipo, valor, c.getSaldo()));
        repositorio.guardar();
    }
}
