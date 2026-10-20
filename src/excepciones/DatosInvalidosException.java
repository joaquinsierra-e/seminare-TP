package src.excepciones;

/** Datos de entrada inválidos o violación de una regla de dominio (CUIT, estados, duplicados, etc.). */
public class DatosInvalidosException extends SeminareException {

    private static final long serialVersionUID = 1L;

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
