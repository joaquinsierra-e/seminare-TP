package src.vista;

import java.io.File;
import java.math.BigDecimal;

import src.controlador.GestorInventario;
import src.controlador.ResultadoImportacion;
import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;
import src.util.Consola;

/** VISTA del CU02 "Gestionar Stock de Semillas". */
public class VistaInventario extends VistaBase {

    private static final String[] OPCIONES = {
        "Ver catálogo de híbridos",
        "Ver lotes (ordenados por poder germinativo)",
        "Buscar lote por número",
        "Registrar ingreso de lote",
        "Resumen de stock por cultivo",
        "Exportar lista de precios a CSV",
        "Importar lista de precios desde CSV (actualiza MySQL)"
    };

    private static final String CSV_POR_DEFECTO = "exportaciones" + File.separator + "lista_precios.csv";

    private final GestorInventario inventario;

    public VistaInventario(Consola consola, GestorInventario inventario) {
        super(consola);
        this.inventario = inventario;
    }

    @Override
    protected String titulo() {
        return "--- CU02 Gestionar Stock de Semillas ---";
    }

    @Override
    protected String[] opciones() {
        return OPCIONES;
    }

    @Override
    protected void atender(int opcion) throws SeminareException {
        switch (opcion) {
            case 1:
                verCatalogo();
                break;
            case 2:
                verLotes();
                break;
            case 3:
                buscarLote();
                break;
            case 4:
                registrarLote();
                break;
            case 5:
                resumenPorCultivo();
                break;
            case 6:
                exportarPrecios();
                break;
            case 7:
                importarPrecios();
                break;
            default:
                break;
        }
    }

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

    /** Recorre la matriz que arma el controlador y agrega la fila de totales. */
    private void resumenPorCultivo() throws SeminareException {
        int[][] resumen = inventario.resumenStockPorCultivo();
        String[] cultivos = GestorInventario.CULTIVOS;
        int totalLotes = 0;
        int totalBolsas = 0;
        System.out.printf("  %-10s %6s %10s%n", "Cultivo", "Lotes", "Bolsas");
        for (int i = 0; i < resumen.length; i++) {
            System.out.printf("  %-10s %6d %10d%n", cultivos[i],
                    resumen[i][GestorInventario.COL_LOTES], resumen[i][GestorInventario.COL_BOLSAS]);
            totalLotes += resumen[i][GestorInventario.COL_LOTES];
            totalBolsas += resumen[i][GestorInventario.COL_BOLSAS];
        }
        System.out.printf("  %-10s %6d %10d%n", "TOTAL", totalLotes, totalBolsas);
    }

    private void exportarPrecios() throws SeminareException {
        File destino = inventario.exportarListaPrecios(new File(CSV_POR_DEFECTO));
        System.out.println("  Lista de precios exportada en: " + destino.getAbsolutePath());
        System.out.println("  Puede editarla en Excel (separador ';') y volver a importarla con la opción 7.");
    }

    private void importarPrecios() throws SeminareException {
        String ruta = consola.leerTexto("Ruta del CSV (Enter = " + CSV_POR_DEFECTO + "): ");
        File origen = new File(ruta.isEmpty() ? CSV_POR_DEFECTO : ruta);
        ResultadoImportacion r = inventario.importarListaPrecios(origen);
        System.out.println("  Precios actualizados: " + r.getActualizados());
        if (!r.getRechazos().isEmpty()) {
            System.out.println("  Filas rechazadas: " + r.getRechazos().size());
            for (String motivo : r.getRechazos()) {
                System.out.println("    - " + motivo);
            }
        }
    }
}
