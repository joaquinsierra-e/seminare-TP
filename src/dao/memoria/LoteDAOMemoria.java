package src.dao.memoria;

import java.util.ArrayList;
import java.util.List;

import src.dao.ILoteDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.LoteInsumo;

/** DAO de lotes sobre {@link AlmacenMemoria}. Devuelve COPIAS, igual que una lectura real de la BD. */
public class LoteDAOMemoria implements ILoteDAO {

    private final AlmacenMemoria almacen;

    public LoteDAOMemoria(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<LoteInsumo> listar() {
        List<LoteInsumo> copia = new ArrayList<>();
        for (LoteInsumo l : almacen.lotes) {
            copia.add(l.copiar());
        }
        return copia;
    }

    @Override
    public LoteInsumo buscarPorId(int id) {
        for (LoteInsumo l : almacen.lotes) {
            if (l.getId() == id) {
                return l.copiar();
            }
        }
        return null;
    }

    @Override
    public void insertar(LoteInsumo lote) throws SeminareException {
        for (LoteInsumo l : almacen.lotes) {
            if (l.getNumeroLote().equalsIgnoreCase(lote.getNumeroLote())) {
                throw new DatosInvalidosException("No se pudo registrar el lote: el número " + lote.getNumeroLote() + " ya existe.");
            }
        }
        lote.setId(almacen.nuevoIdLote());
        almacen.lotes.add(lote.copiar());
    }
}
