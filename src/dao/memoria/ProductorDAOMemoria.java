package src.dao.memoria;

import java.util.ArrayList;
import java.util.List;

import src.dao.IProductorDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.Productor;

/** DAO de productores sobre {@link AlmacenMemoria}. */
public class ProductorDAOMemoria implements IProductorDAO {

    private final AlmacenMemoria almacen;

    public ProductorDAOMemoria(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    @Override
    public List<Productor> listar() {
        return new ArrayList<>(almacen.productores);
    }

    @Override
    public Productor buscarPorId(int id) {
        for (Productor p : almacen.productores) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void insertar(Productor p) throws SeminareException {
        for (Productor existente : almacen.productores) {
            if (existente.getCuit().equals(p.getCuit())) {
                throw new DatosInvalidosException("No se pudo registrar el productor: el CUIT " + p.getCuit() + " ya existe.");
            }
        }
        p.setId(almacen.nuevoIdProductor());
        almacen.productores.add(p);
    }
}
