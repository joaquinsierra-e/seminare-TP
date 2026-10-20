package src.excepciones;

/** Error técnico de la capa de persistencia (envuelve las SQLException de JDBC). */
public class PersistenciaException extends SeminareException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
