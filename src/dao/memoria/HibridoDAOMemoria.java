package src.dao.memoria;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import src.dao.IHibridoDAO;
import src.excepciones.DatosInvalidosException;
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

    @Override
    public HibridoNidera buscarPorCodigo(String codigoVariedad) {
        for (HibridoNidera h : almacen.hibridos) {
            if (h.getCodigoVariedad().equalsIgnoreCase(codigoVariedad.trim())) {
                return h;
            }
        }
        return null;
    }

    @Override
    public boolean actualizarPrecio(int id, BigDecimal precioUsd) throws DatosInvalidosException {
        HibridoNidera h = buscarPorId(id);
        if (h == null) {
            return false;
        }
        h.actualizarPrecio(precioUsd);
        return true;
    }
}
