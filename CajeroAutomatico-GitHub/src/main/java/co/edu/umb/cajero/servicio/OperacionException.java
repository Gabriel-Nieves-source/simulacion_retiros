package co.edu.umb.cajero.servicio;

/**
 * Error de negocio del cajero (saldo insuficiente, monto inválido, PIN incorrecto, etc.).
 * Su mensaje se muestra directamente al usuario.
 */
public class OperacionException extends Exception {
    public OperacionException(String mensaje) {
        super(mensaje);
    }
}
