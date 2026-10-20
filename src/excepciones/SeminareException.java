package src.excepciones;

/**
 * Excepción base (checked) del sistema Seminare.
 * Todas las excepciones propias heredan de ella, de modo que la capa de
 * presentación pueda capturar "cualquier error controlado" con un único catch.
 */
public class SeminareException extends Exception {

    private static final long serialVersionUID = 1L;

    public SeminareException(String mensaje) {
        super(mensaje);
    }

    public SeminareException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
