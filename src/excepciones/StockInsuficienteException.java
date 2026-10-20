package src.excepciones;

/** Regla de negocio: se pidió más cantidad que la disponible en el lote (CP-02). */
public class StockInsuficienteException extends SeminareException {

    private static final long serialVersionUID = 1L;

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }

    public StockInsuficienteException(String nroLote, int disponible, int solicitado) {
        super("Stock Insuficiente en el lote " + nroLote + ": disponible=" + disponible
                + ", solicitado=" + solicitado + ".");
    }
}
