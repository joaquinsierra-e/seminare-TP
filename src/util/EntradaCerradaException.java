package src.util;

/** Se cerró la entrada estándar (fin de archivo / Ctrl+D): el programa debe terminar ordenadamente. */
public class EntradaCerradaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EntradaCerradaException() {
        super("Entrada cerrada");
    }
}
