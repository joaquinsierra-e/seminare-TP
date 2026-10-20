package src.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import src.excepciones.DatosInvalidosException;
import src.excepciones.PersistenciaException;
import src.excepciones.SeminareException;

/**
 * Punto único de acceso a MySQL. Los valores por defecto coinciden con el entorno local del proyecto
 * y pueden sobrescribirse con las variables de entorno SEMINARE_DB_URL, SEMINARE_DB_USER y SEMINARE_DB_PASS.
 */
public final class ConexionDB {

    private static final String URL = env("SEMINARE_DB_URL",
            "jdbc:mysql://localhost:3306/seminare_db?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=America/Argentina/Cordoba&characterEncoding=UTF-8");
    private static final String USER = env("SEMINARE_DB_USER", "root");
    private static final String PASS = env("SEMINARE_DB_PASS", "root");

    private ConexionDB() {
    }

    private static String env(String clave, String porDefecto) {
        String valor = System.getenv(clave);
        return (valor == null || valor.isBlank()) ? porDefecto : valor;
    }

    public static Connection obtener() throws PersistenciaException {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a MySQL (" + e.getMessage() + ")", e);
        }
    }

    /** Verifica la conectividad; devuelve el mensaje de error o {@code null} si todo está bien. */
    public static String probar() {
        try (Connection c = obtener()) {
            return c.isValid(2) ? null : "La conexión no es válida.";
        } catch (SeminareException e) {
            return e.getMessage();
        } catch (SQLException e) {
            return e.getMessage();
        }
    }

    /** Traduce una SQLException a la excepción de dominio adecuada. */
    static SeminareException traducir(SQLException e, String contexto) {
        if (e instanceof SQLIntegrityConstraintViolationException) {
            return new DatosInvalidosException(contexto
                    + ": viola una restricción de integridad (valor duplicado, clave foránea o CHECK).");
        }
        return new PersistenciaException(contexto + ": " + e.getMessage(), e);
    }
}
