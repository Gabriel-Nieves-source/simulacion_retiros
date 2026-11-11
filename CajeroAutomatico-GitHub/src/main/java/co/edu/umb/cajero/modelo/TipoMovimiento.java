package co.edu.umb.cajero.modelo;

/**
 * Tipos de operación que puede registrar el cajero.
 */
public enum TipoMovimiento {
    RETIRO("Retiro"),
    CONSIGNACION("Consignación"),
    CONSULTA("Consulta de saldo");

    private final String descripcion;

    TipoMovimiento(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
