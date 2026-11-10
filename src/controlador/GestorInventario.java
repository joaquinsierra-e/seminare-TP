package src.controlador;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.archivos.ArchivoListaPrecios;

import src.dao.IHibridoDAO;
import src.dao.ILoteDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;
import src.util.Algoritmos;

/**
 * CONTROLADOR (MVC) del CU02 "Gestionar Stock de Semillas": catálogo, lotes, ordenamiento, búsqueda,
 * resumen de stock por cultivo y lista de precios en archivo CSV.
 */
public class GestorInventario {

    /** Cultivos que comercializa Seminare. El índice de cada cultivo es la fila del resumen de stock. */
    public static final String[] CULTIVOS = {"Maíz", "Soja", "Girasol"};

    /** Columnas del resumen de stock (int[][]). */
    public static final int COL_LOTES = 0;
    public static final int COL_BOLSAS = 1;

    private final IHibridoDAO hibridoDAO;
    private final ILoteDAO loteDAO;
    private final ArchivoListaPrecios archivoPrecios = new ArchivoListaPrecios();

    public GestorInventario(IHibridoDAO hibridoDAO, ILoteDAO loteDAO) {
        this.hibridoDAO = hibridoDAO;
        this.loteDAO = loteDAO;
    }

    public List<HibridoNidera> listarCatalogo() throws SeminareException {
        return hibridoDAO.listar();
    }

    /** Lotes ordenados de mayor a menor poder germinativo (ordenación por inserción). */
    public List<LoteInsumo> listarLotesPorGerminacion() throws SeminareException {
        List<LoteInsumo> lotes = new ArrayList<>(loteDAO.listar());
        Algoritmos.ordenarPorInsercion(lotes,
                (a, b) -> Double.compare(b.getPoderGerminativo(), a.getPoderGerminativo()));
        return lotes;
    }

    /** Lotes con bolsas disponibles (para ofrecer en una preventa). */
    public List<LoteInsumo> listarLotesConStock() throws SeminareException {
        List<LoteInsumo> conStock = new ArrayList<>();
        for (LoteInsumo l : loteDAO.listar()) {
            if (l.getBolsasDisponibles() > 0) {
                conStock.add(l);
            }
        }
        return conStock;
    }

    /** Busca un lote por su número: ordena por número y aplica búsqueda binaria. */
    public LoteInsumo buscarLotePorNumero(String numero) throws SeminareException {
        if (numero == null || numero.isBlank()) {
            throw new DatosInvalidosException("Ingrese un número de lote.");
        }
        List<LoteInsumo> lotes = new ArrayList<>(loteDAO.listar());
        Algoritmos.ordenarPorSeleccion(lotes, (a, b) -> a.getNumeroLote().compareToIgnoreCase(b.getNumeroLote()));
        int pos = Algoritmos.busquedaBinaria(lotes, numero.trim().toUpperCase(), l -> l.getNumeroLote().toUpperCase());
        return pos >= 0 ? lotes.get(pos) : null;
    }

    public LoteInsumo registrarIngresoLote(int idHibrido, String numeroLote, int bolsas, double poderGerminativo)
            throws SeminareException {
        HibridoNidera hibrido = hibridoDAO.buscarPorId(idHibrido);
        if (hibrido == null) {
            throw new DatosInvalidosException("No existe el híbrido #" + idHibrido + " en el catálogo.");
        }
        LoteInsumo lote = new LoteInsumo(0, numeroLote, hibrido, bolsas, poderGerminativo);
        loteDAO.insertar(lote);
        return lote;
    }

    /**
     * Resumen del depósito por cultivo usando una MATRIZ: fila = cultivo (mismo índice que CULTIVOS),
     * columna 0 = cantidad de lotes, columna 1 = bolsas disponibles. Los lotes llegan en un ArrayList
     * (tamaño variable, viene de la BD) y se acumulan en el arreglo (tamaño fijo: 3 cultivos x 2 datos).
     */
    public int[][] resumenStockPorCultivo() throws SeminareException {
        int[][] resumen = new int[CULTIVOS.length][2]; // Java inicializa todo en 0
        List<LoteInsumo> lotes = loteDAO.listar();
        for (LoteInsumo lote : lotes) {
            int fila = indiceCultivo(lote.getHibrido().getCultivo());
            if (fila >= 0) {
                resumen[fila][COL_LOTES]++;
                resumen[fila][COL_BOLSAS] += lote.getBolsasDisponibles();
            }
        }
        return resumen;
    }

    /** Búsqueda lineal del cultivo en el arreglo CULTIVOS; -1 si no está. */
    static int indiceCultivo(String cultivo) {
        for (int i = 0; i < CULTIVOS.length; i++) {
            if (CULTIVOS[i].equalsIgnoreCase(cultivo)) {
                return i;
            }
        }
        return -1;
    }

    /** Exporta el catálogo con sus precios a un CSV que se puede abrir y editar en Excel. */
    public File exportarListaPrecios(File destino) throws SeminareException {
        archivoPrecios.exportar(hibridoDAO.listar(), destino);
        return destino;
    }

    /**
     * Lee el CSV y actualiza en MySQL el precio de cada híbrido. Una fila con error no frena a las demás:
     * se informa en el resultado. Un error de archivo o de base de datos sí corta el proceso (excepción).
     */
    public ResultadoImportacion importarListaPrecios(File origen) throws SeminareException {
        ResultadoImportacion resultado = new ResultadoImportacion();
        List<String[]> filas = archivoPrecios.leerFilas(origen);
        int numeroLinea = 1; // la línea 1 es el encabezado
        for (String[] campos : filas) {
            numeroLinea++;
            if (campos.length != ArchivoListaPrecios.ENCABEZADO.length) {
                resultado.rechazar(numeroLinea, "se esperaban " + ArchivoListaPrecios.ENCABEZADO.length
                        + " campos separados por ';' y hay " + campos.length + ".");
                continue;
            }
            String codigo = campos[0].trim();
            HibridoNidera hibrido = hibridoDAO.buscarPorCodigo(codigo);
            if (hibrido == null) {
                resultado.rechazar(numeroLinea, "el híbrido '" + codigo + "' no está en el catálogo.");
                continue;
            }
            try {
                BigDecimal precio = new BigDecimal(campos[2].trim().replace(',', '.'));
                hibrido.actualizarPrecio(precio); // valida que sea mayor a cero (regla del modelo)
                hibridoDAO.actualizarPrecio(hibrido.getId(), hibrido.getPrecioUsd());
                resultado.sumarActualizado();
            } catch (NumberFormatException e) {
                resultado.rechazar(numeroLinea, "'" + campos[2].trim() + "' no es un precio válido.");
            } catch (DatosInvalidosException e) {
                resultado.rechazar(numeroLinea, e.getMessage());
            }
        }
        return resultado;
    }
}
