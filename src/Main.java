package src;

import src.controlador.GestorDespacho;
import src.controlador.GestorInventario;
import src.controlador.GestorPreventa;
import src.dao.ConexionDB;
import src.dao.FabricaDAO;
import src.util.Consola;
import src.util.EntradaCerradaException;
import src.vista.VistaDespacho;
import src.vista.VistaInventario;
import src.vista.VistaPreventa;
import src.vista.VistaPrincipal;

/**
 * Punto de entrada. Arma las tres partes del patrón MVC y las conecta:
 * MODELO (src.modelo + src.dao) -> CONTROLADOR (src.controlador) -> VISTA (src.vista).
 * Ejecución normal: contra MySQL. Con --memoria: datos de demostración sin base de datos.
 * Con --mysql: si no hay conexión termina (no pregunta), para la prueba automática de demo.bat.
 */
public class Main {

    public static void main(String[] args) {
        Consola consola = new Consola();
        try {
            FabricaDAO dao;
            String origenDatos;
            boolean forzarMemoria = args.length > 0 && args[0].equalsIgnoreCase("--memoria");
            boolean soloMysql = args.length > 0 && args[0].equalsIgnoreCase("--mysql"); // lo usa demo.bat
            if (forzarMemoria) {
                dao = FabricaDAO.memoria();
                origenDatos = "MEMORIA (demostración, sin MySQL)";
            } else {
                System.out.println("Configuración de MySQL tomada de: " + ConexionDB.origenConfiguracion());
                String error = ConexionDB.probar();
                if (error == null) {
                    dao = FabricaDAO.jdbc();
                    origenDatos = "MySQL (seminare_db)";
                } else {
                    System.out.println("No se pudo conectar a MySQL: " + error);
                    System.out.println("Verifique que el servicio esté activo, que exista seminare_db, los datos de"
                            + " config/db.properties y que el driver esté en la carpeta lib/.");
                    if (soloMysql || !consola.confirmar("¿Continuar en modo demostración (datos en memoria)?")) {
                        return;
                    }
                    dao = FabricaDAO.memoria();
                    origenDatos = "MEMORIA (demostración, sin MySQL)";
                }
            }

            // Controladores: reciben los DAO por constructor (no saben si es MySQL o memoria)
            GestorInventario inventario = new GestorInventario(dao.hibridos(), dao.lotes());
            GestorPreventa preventa = new GestorPreventa(dao.productores(), dao.lotes(), dao.ordenes());
            GestorDespacho despacho = new GestorDespacho(dao.ordenes());

            // Vistas: reciben los controladores
            VistaPrincipal principal = new VistaPrincipal(consola, origenDatos,
                    new VistaPreventa(consola, preventa, inventario),
                    new VistaInventario(consola, inventario),
                    new VistaDespacho(consola, despacho));
            principal.mostrar();
            System.out.println("Hasta luego.");
        } catch (EntradaCerradaException e) {
            System.out.println();
            System.out.println("Entrada finalizada. Hasta luego.");
        }
    }
}
