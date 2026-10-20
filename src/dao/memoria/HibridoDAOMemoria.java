package src.dao.memoria;

import java.util.ArrayList;
import java.util.List;

import src.dao.IHibridoDAO;
import src.modelo.HibridoNidera;

/** DAO del catálogo sobre {@link AlmacenMemoria}. */
public class HibridoDAOMemoria implements IHibridoDAO {

    private final AlmacenMemoria almacen;

    public HibridoDAOMemoria(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<HibridoNidera> listar() {
        return new ArrayList<>(almacen.hibridos);
    }

    @Override
    public HibridoNidera buscarPorId(int id) {
        for (HibridoNidera h : almacen.hibridos) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }
}
