package src;

import java.math.BigDecimal;
import java.util.List;

import src.controlador.GestorDespacho;
import src.controlador.GestorInventario;
import src.controlador.GestorPreventa;
import src.dao.ConexionDB;
import src.dao.FabricaDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.PersistenciaException;
import src.excepciones.SeminareException;
import src.excepciones.StockInsuficienteException;
import src.modelo.CondicionPago;
import src.modelo.DetalleOrden;
import src.modelo.EstadoOrden;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;
import src.modelo.Productor;
import src.util.Consola;
import src.util.EntradaCerradaException;

/**
 * Punto de entrada y capa de PRESENTACIÓN (clase Boundary) del prototipo de Seminare S.R.L.
 * Ejecución normal: contra MySQL.  Con el argumento --memoria: datos de demostración sin base de datos.
 */
public class Main {

    /** Acción de menú que puede fallar con una excepción controlada del sistema. */
    private interface Accion {
        void ejecutar() throws SeminareException;
    }

    private final Consola consola = new Consola();
    private final GestorInventario inventario;
    private final GestorPreventa preventa;
    private final GestorDespacho despacho;
    private final String origenDatos;

    private Main(FabricaDAO dao, String origenDatos) {
        this.inventario = new GestorInventario(dao.hibridos(), dao.lotes());
        this.preventa = new GestorPreventa(dao.productores(), dao.lotes(), dao.ordenes());
        this.despacho = new GestorDespacho(dao.ordenes());
        this.origenDatos = origenDatos;
    }

    public static void main(String[] args) {
        try {
            boolean forzarMemoria = args.length > 0 && args[0].equalsIgnoreCase("--memoria");
            Main app;
            if (forzarMemoria) {
                app = new Main(FabricaDAO.memoria(), "MEMORIA (demostración, sin MySQL)");
            } else {
                String error = ConexionDB.probar();
                if (error == null) {
                    app = new Main(FabricaDAO.jdbc(), "MySQL (seminare_db)");
                } else {
                    System.out.println("No se pudo conectar a MySQL: " + error);
                    System.out.println("Verifique que el servicio esté activo, que exista seminare_db y que el driver"
                            + " mysql-connector-j esté en el classpath (carpeta lib/).");
                    if (!new Consola().confirmar("¿Continuar en modo demostración (datos en memoria)?")) {
                        return;
                    }
                    app = new Main(FabricaDAO.memoria(), "MEMORIA (demostración, sin MySQL)");
                }
            }
            app.menuPrincipal();
        } catch (EntradaCerradaException e) {
            System.out.println();
            System.out.println("Entrada finalizada. Hasta luego.");
        }
    }

    // ===================== MENÚS =====================

    private void menuPrincipal() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== SEMINARE S.R.L. - Gestión Comercial (Prototipo TP3) ===");
            System.out.println("Fuente de datos: " + origenDatos);
            System.out.println("1. Registrar preventa            (CU01)");
            System.out.println("2. Gestionar stock de semillas   (CU02)");
            System.out.println("3. Despacho y logística          (CU03)");
            System.out.println("0. Salir");
            opcion = consola.leerEntero("Opción: ", 0, 3);
            switch (opcion) {
                case 1:
                    menuPreventa();
                    break;
                case 2:
                    menuStock();
                    break;
                case 3:
                    menuDespacho();
                    break;
                default:
                    System.out.println("Hasta luego.");
            }
        } while (opcion != 0);
    }

    private void menuPreventa() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- CU01 Registrar Preventa ---");
            System.out.println("1. Nueva preventa");
            System.out.println("2. Registrar productor");
            System.out.println("3. Listar productores");
            System.out.println("4. Listar preventas por estado");
            System.out.println("5. Cancelar la última preventa de la sesión (pila)");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ", 0, 5);
            switch (opcion) {
                case 1:
                    ejecutar(this::nuevaPreventa);
                    break;
                case 2:
                    ejecutar(this::registrarProductor);
                    break;
                case 3:
                    ejecutar(this::listarProductores);
                    break;
                case 4:
                    ejecutar(this::listarPreventas);
                    break;
                case 5:
                    ejecutar(this::cancelarUltima);
                    break;
                default:
                    break;
            }
        } while (opcion != 0);
    }

    private void menuStock() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- CU02 Gestionar Stock de Semillas ---");
            System.out.println("1. Ver catálogo de híbridos");
            System.out.println("2. Ver lotes (ordenados por poder germinativo)");
            System.out.println("3. Buscar lote por número");
            System.out.println("4. Registrar ingreso de lote");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ", 0, 4);
            switch (opcion) {
                case 1:
                    ejecutar(this::verCatalogo);
                    break;
                case 2:
                    ejecutar(this::verLotes);
                    break;
                case 3:
                    ejecutar(this::buscarLote);
                    break;
                case 4:
                    ejecutar(this::registrarLote);
                    break;
                default:
                    break;
            }
        } while (opcion != 0);
    }

    private void menuDespacho() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- CU03 Despacho y Logística ---");
            System.out.println("1. Ver cola de despacho (orden de llegada)");
            System.out.println("2. Despachar la siguiente orden");
            System.out.println("0. Volver");
            opcion = consola.leerEntero("Opción: ", 0, 2);
            switch (opcion) {
                case 1:
                    ejecutar(this::verCola);
                    break;
                case 2:
                    ejecutar(this::despacharSiguiente);
                    break;
                default:
                    break;
            }
        } while (opcion != 0);
    }

    /** Manejo centralizado de excepciones: el usuario ve un mensaje claro y el programa sigue funcionando. */
    private void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (StockInsuficienteException e) {
            System.out.println("  [STOCK] " + e.getMessage());
        } catch (DatosInvalidosException e) {
            System.out.println("  [DATOS] " + e.getMessage());
        } catch (PersistenciaException e) {
            System.out.println("  [BASE DE DATOS] " + e.getMessage());
        } catch (SeminareException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    // ===================== CU01 =====================

    private void nuevaPreventa() throws SeminareException {
        listarProductores();
        int idProductor = consola.leerEntero("ID del productor: ", 1, Integer.MAX_VALUE);

        System.out.println("Condición de pago:");
        CondicionPago[] condiciones = CondicionPago.values();
        for (int i = 0; i < condiciones.length; i++) {
            System.out.println("  " + (i + 1) + ". " + condiciones[i].getDescripcion());
        }
        CondicionPago condicion = condiciones[consola.leerEntero("Opción: ", 1, condiciones.length) - 1];
        BigDecimal cotizacion = consola.leerDecimal("Cotización del dólar (ARS por USD): ", BigDecimal.ZERO);

        OrdenPreventa orden = preventa.iniciarOrden(idProductor, condicion, cotizacion);

        boolean seguir = true;
        while (seguir) {
            List<LoteInsumo> disponibles = inventario.listarLotesConStock();
            if (disponibles.isEmpty()) {
                System.out.println("  No hay lotes con stock disponible.");
                break;
            }
            System.out.println("Lotes con stock:");
            for (LoteInsumo l : disponibles) {
                System.out.println("  " + l);
            }
            int idLote = consola.leerEntero("ID del lote: ", 1, Integer.MAX_VALUE);
            int cantidad = consola.leerEntero("Cantidad de bolsas: ", 1, Integer.MAX_VALUE);
            try {
                DetalleOrden d = preventa.agregarItem(orden, idLote, cantidad);
                System.out.println("  Ítem agregado:");
                System.out.println(d);
            } catch (SeminareException e) {
                System.out.println("  [!] " + e.getMessage());
            }
            seguir = consola.confirmar("¿Agregar otro lote a la orden?");
        }

        if (orden.getDetalles().isEmpty()) {
            System.out.println("  Orden sin ítems: no se registró nada.");
            return;
        }
        System.out.println();
        System.out.println("Resumen de la orden:");
        System.out.println(orden);
        if (consola.confirmar("¿Confirmar la preventa?")) {
            preventa.confirmar(orden);
            System.out.println("  Preventa #" + orden.getId() + " registrada en estado " + orden.getEstado() + ".");
        } else {
            System.out.println("  Preventa descartada; el stock no fue modificado.");
        }
    }

    private void registrarProductor() throws SeminareException {
        String cuit = consola.leerTextoNoVacio("CUIT (XX-XXXXXXXX-X): ");
        String razon = consola.leerTextoNoVacio("Razón social: ");
        String localidad = consola.leerTexto("Localidad: ");
        String telefono = consola.leerTexto("Teléfono: ");
        Productor p = preventa.registrarProductor(cuit, razon, localidad, telefono);
        System.out.println("  Productor registrado: " + p);
    }

    private void listarProductores() throws SeminareException {
        System.out.println("Productores:");
        for (Productor p : preventa.listarProductores()) {
            System.out.println("  " + p);
        }
    }

    private void listarPreventas() throws SeminareException {
        EstadoOrden[] estados = EstadoOrden.values();
        for (int i = 0; i < estados.length; i++) {
            System.out.println("  " + (i + 1) + ". " + estados[i]);
        }
        EstadoOrden estado = estados[consola.leerEntero("Estado a listar: ", 1, estados.length) - 1];
        List<OrdenPreventa> ordenes = preventa.listarPreventas(estado);
        if (ordenes.isEmpty()) {
            System.out.println("  No hay órdenes en estado " + estado + ".");
        }
        for (OrdenPreventa o : ordenes) {
            System.out.println(o);
            System.out.println();
        }
    }

    private void cancelarUltima() throws SeminareException {
        System.out.println("Preventas de esta sesión: " + preventa.cantidadEnHistorial());
        OrdenPreventa o = preventa.cancelarUltimaPreventa();
        System.out.println("  Preventa #" + o.getId() + " cancelada; las bolsas volvieron al stock.");
    }

    // ===================== CU02 =====================

    private void verCatalogo() throws SeminareException {
        for (HibridoNidera h : inventario.listarCatalogo()) {
            System.out.println("  " + h);
        }
    }

    private void verLotes() throws SeminareException {
        for (LoteInsumo l : inventario.listarLotesPorGerminacion()) {
            System.out.println("  " + l);
        }
    }

    private void buscarLote() throws SeminareException {
        String numero = consola.leerTextoNoVacio("Número de lote: ");
        LoteInsumo l = inventario.buscarLotePorNumero(numero);
        System.out.println(l == null ? "  No se encontró el lote '" + numero + "'." : "  " + l);
    }

    private void registrarLote() throws SeminareException {
        verCatalogo();
        int idHibrido = consola.leerEntero("ID del híbrido: ", 1, Integer.MAX_VALUE);
        String numero = consola.leerTextoNoVacio("Número de lote: ");
        int bolsas = consola.leerEntero("Bolsas ingresadas: ", 0, Integer.MAX_VALUE);
        double pg = consola.leerDecimal("Poder germinativo (%): ", new BigDecimal("-1")).doubleValue();
        LoteInsumo l = inventario.registrarIngresoLote(idHibrido, numero, bolsas, pg);
        System.out.println("  Lote registrado: " + l);
    }

    // ===================== CU03 =====================

    private void verCola() throws SeminareException {
        List<OrdenPreventa> cola = despacho.verCola();
        if (cola.isEmpty()) {
            System.out.println("  No hay órdenes pendientes de despacho.");
            return;
        }
        int posicion = 1;
        for (OrdenPreventa o : cola) {
            System.out.println("Posición " + posicion++ + ":");
            System.out.println(o);
            System.out.println();
        }
    }

    private void despacharSiguiente() throws SeminareException {
        List<OrdenPreventa> cola = despacho.verCola();
        if (cola.isEmpty()) {
            System.out.println("  No hay órdenes pendientes de despacho.");
            return;
        }
        System.out.println("Se despachará:");
        System.out.println(cola.get(0));
        String patente = consola.leerTextoNoVacio("Patente del camión (ABC123 o AB123CD): ");
        OrdenDespacho d = despacho.despacharSiguiente(patente);
        System.out.println("  Despacho registrado -> " + d);
    }
}
