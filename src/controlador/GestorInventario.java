package src.controlador;

import java.util.ArrayList;
import java.util.List;

import src.dao.IHibridoDAO;
import src.dao.ILoteDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;
import src.util.Algoritmos;

/** Clase de CONTROL del CU02 "Gestionar Stock de Semillas": catálogo, lotes, ordenamiento y búsqueda. */
public class GestorInventario {

    private final IHibridoDAO hibridoDAO;
    private final ILoteDAO loteDAO;

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
}
