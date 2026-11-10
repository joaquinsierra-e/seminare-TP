package src.archivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Lee los datos de conexión desde el archivo de texto config/db.properties (clave=valor).
 * Así el usuario y la contraseña de MySQL no quedan escritos en el código ni se suben a GitHub.
 * Si el archivo no existe o no se puede leer, se usan los valores por defecto del entorno local.
 */
public final class ConfiguracionDB {

    public static final String RUTA = "config" + File.separator + "db.properties";

    private static final String URL_DEFECTO = "jdbc:mysql://localhost:3306/seminare_db?useSSL=false"
            + "&allowPublicKeyRetrieval=true&serverTimezone=America/Argentina/Cordoba&characterEncoding=UTF-8";

    private final String url;
    private final String usuario;
    private final String clave;
    private final String origen; // para informar al usuario de dónde salió la configuración

    private ConfiguracionDB(String url, String usuario, String clave, String origen) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
        this.origen = origen;
    }

    public static ConfiguracionDB cargar() {
        File archivo = new File(RUTA);
        if (!archivo.exists()) {
            return new ConfiguracionDB(URL_DEFECTO, "root", "root", "valores por defecto (no existe " + RUTA + ")");
        }
        Properties props = new Properties();
        // try-with-resources: el flujo se cierra solo, aunque ocurra una excepción
        try (InputStream entrada = new FileInputStream(archivo)) {
            props.load(entrada);
        } catch (IOException e) {
            return new ConfiguracionDB(URL_DEFECTO, "root", "root",
                    "valores por defecto (error al leer " + RUTA + ": " + e.getMessage() + ")");
        }
        return new ConfiguracionDB(
                props.getProperty("db.url", URL_DEFECTO).trim(),
                props.getProperty("db.usuario", "root").trim(),
                props.getProperty("db.clave", "").trim(),
                RUTA);
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getClave() {
        return clave;
    }

    public String getOrigen() {
        return origen;
    }
}
