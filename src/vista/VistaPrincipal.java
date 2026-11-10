package src.vista;

import src.util.Consola;

/** Menú principal: deriva a la vista de cada caso de uso. */
public class VistaPrincipal extends VistaBase {

    private static final String[] OPCIONES = {
        "Registrar preventa            (CU01)",
        "Gestionar stock de semillas   (CU02)",
        "Despacho y logística          (CU03)"
    };

    private final String origenDatos;
    private final VistaBase[] subvistas; // mismo orden que OPCIONES

    public VistaPrincipal(Consola consola, String origenDatos, VistaPreventa preventa,
                          VistaInventario inventario, VistaDespacho despacho) {
        super(consola);
        this.origenDatos = origenDatos;
        this.subvistas = new VistaBase[] {preventa, inventario, despacho};
    }

    @Override
    protected String titulo() {
        return "=== SEMINARE S.R.L. - Gestión Comercial (versión final TP4) ===\nFuente de datos: " + origenDatos;
    }

    @Override
    protected String[] opciones() {
        return OPCIONES;
    }

    @Override
    protected String textoOpcionCero() {
        return "Salir";
    }

    @Override
    protected void atender(int opcion) {
        subvistas[opcion - 1].mostrar(); // POLIMORFISMO: cada subvista sabe mostrarse
    }
}
