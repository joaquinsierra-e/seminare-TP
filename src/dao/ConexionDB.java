package src.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import src.archivos.ConfiguracionDB;
import src.excepciones.DatosInvalidosException;
import src.excepciones.PersistenciaException;
import src.excepciones.SeminareException;

/**
 * Punto único de acceso a MySQL (JDBC): DriverManager entrega la Connection con los datos de
 * config/db.properties. Cada DAO pide una conexión, la usa y la cierra con try-with-resources.
 */
public final class ConexionDB {

    /** Datos de conexión leídos una sola vez del archivo config/db.properties (ver ConfiguracionDB). */
    private static final ConfiguracionDB CONFIG = ConfiguracionDB.cargar();

    private ConexionDB() {
    }

    /** Indica de dónde se tomó la configuración (se muestra al iniciar el programa). */
    public static String origenConfiguracion() {
        return CONFIG.getOrigen();
    }

    public static Connection obtener() throws PersistenciaException {
        try {
            return DriverManager.getConnection(CONFIG.getUrl(), CONFIG.getUsuario(), CONFIG.getClave());
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
