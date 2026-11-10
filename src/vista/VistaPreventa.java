package src.vista;

import java.math.BigDecimal;
import java.util.List;

import src.controlador.GestorInventario;
import src.controlador.GestorPreventa;
import src.excepciones.SeminareException;
import src.modelo.CondicionPago;
import src.modelo.DetalleOrden;
import src.modelo.EstadoOrden;
import src.modelo.LoteInsumo;
import src.modelo.OrdenPreventa;
import src.modelo.Productor;
import src.util.Consola;

/** VISTA del CU01 "Registrar Preventa". Solo pide datos y muestra resultados; las reglas están en GestorPreventa. */
public class VistaPreventa extends VistaBase {

    private static final String[] OPCIONES = {
        "Nueva preventa",
        "Registrar productor",
        "Listar productores",
        "Listar preventas por estado",
        "Cancelar la última preventa de la sesión (pila)"
    };

    private final GestorPreventa preventa;
    private final GestorInventario inventario;

    public VistaPreventa(Consola consola, GestorPreventa preventa, GestorInventario inventario) {
        super(consola);
        this.preventa = preventa;
        this.inventario = inventario;
    }

    @Override
    protected String titulo() {
        return "--- CU01 Registrar Preventa ---";
    }

    @Override
    protected String[] opciones() {
        return OPCIONES;
    }

    @Override
    protected void atender(int opcion) throws SeminareException {
        switch (opcion) {
            case 1:
                nuevaPreventa();
                break;
            case 2:
                registrarProductor();
                break;
            case 3:
                listarProductores();
                break;
            case 4:
                listarPreventas();
                break;
            case 5:
                cancelarUltima();
                break;
            default:
                break;
        }
    }

    private void nuevaPreventa() throws SeminareException {
        listarProductores();
        int idProductor = consola.leerEntero("ID del productor: ", 1, Integer.MAX_VALUE);

        System.out.println("Condición de pago:");
        CondicionPago[] condiciones = CondicionPago.values(); // values() devuelve un arreglo
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
                // el error de un ítem no cancela la orden: se avisa y se puede cargar otro
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
}
