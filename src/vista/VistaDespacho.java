package src.vista;

import java.io.File;
import java.util.List;

import src.controlador.GestorDespacho;
import src.excepciones.SeminareException;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;
import src.util.Consola;

/** VISTA del CU03 "Despacho y Logística". */
public class VistaDespacho extends VistaBase {

    private static final String[] OPCIONES = {
        "Ver cola de despacho (orden de llegada)",
        "Despachar la siguiente orden (genera remito .txt)",
        "Ver remitos guardados"
    };

    private final GestorDespacho despacho;

    public VistaDespacho(Consola consola, GestorDespacho despacho) {
        super(consola);
        this.despacho = despacho;
    }

    @Override
    protected String titulo() {
        return "--- CU03 Despacho y Logística ---";
    }

    @Override
    protected String[] opciones() {
        return OPCIONES;
    }

    @Override
    protected void atender(int opcion) throws SeminareException {
        switch (opcion) {
            case 1:
                verCola();
                break;
            case 2:
                despacharSiguiente();
                break;
            case 3:
                verRemitos();
                break;
            default:
                break;
        }
    }

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
        // El despacho ya quedó en MySQL; si falla el archivo se avisa aparte, sin deshacer el despacho.
        ejecutar(() -> {
            File archivo = despacho.generarRemito(d);
            System.out.println("  Remito guardado en: " + archivo.getAbsolutePath());
        });
    }

    private void verRemitos() throws SeminareException {
        String[] remitos = despacho.listarRemitos();
        if (remitos.length == 0) {
            System.out.println("  Todavía no hay remitos guardados.");
            return;
        }
        for (int i = 0; i < remitos.length; i++) {
            System.out.println("  " + (i + 1) + ". " + remitos[i]);
        }
        int elegido = consola.leerEntero("Remito a ver (0 = ninguno): ", 0, remitos.length);
        if (elegido == 0) {
            return;
        }
        System.out.println();
        for (String linea : despacho.leerRemito(remitos[elegido - 1])) {
            System.out.println("  " + linea);
        }
    }
}
