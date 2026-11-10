package src.excepciones;

/** Error al leer o escribir un archivo (envuelve las IOException de java.io / java.nio). */
public class ArchivoException extends SeminareException {

    private static final long serialVersionUID = 1L;

    public ArchivoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ArchivoException(String mensaje) {
        super(mensaje);
    }
}
