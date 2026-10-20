package src.modelo;

/** Condición comercial de la preventa (RF-03). */
public enum CondicionPago {
    CONTADO("Contado"),
    CANJE_GRANOS("Canje de granos"),
    PLAZO("A plazo");

    private final String descripcion;

    CondicionPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
