package src.dao;

import src.dao.memoria.AlmacenMemoria;
import src.dao.memoria.HibridoDAOMemoria;
import src.dao.memoria.LoteDAOMemoria;
import src.dao.memoria.OrdenDAOMemoria;
import src.dao.memoria.ProductorDAOMemoria;

/** Fábrica que entrega el juego completo de DAOs: contra MySQL (JDBC) o en memoria (demostración). */
public final class FabricaDAO {

    private final IProductorDAO productorDAO;
    private final IHibridoDAO hibridoDAO;
    private final ILoteDAO loteDAO;
    private final IOrdenDAO ordenDAO;

    private FabricaDAO(IProductorDAO p, IHibridoDAO h, ILoteDAO l, IOrdenDAO o) {
        this.productorDAO = p;
        this.hibridoDAO = h;
        this.loteDAO = l;
        this.ordenDAO = o;
    }

    public static FabricaDAO jdbc() {
        return new FabricaDAO(new ProductorDAO(), new HibridoDAO(), new LoteDAO(), new OrdenDAO());
    }

    public static FabricaDAO memoria() {
        AlmacenMemoria almacen = AlmacenMemoria.conDatosDemo();
        return new FabricaDAO(new ProductorDAOMemoria(almacen), new HibridoDAOMemoria(almacen),
                new LoteDAOMemoria(almacen), new OrdenDAOMemoria(almacen));
    }

    public IProductorDAO productores() {
        return productorDAO;
    }

    public IHibridoDAO hibridos() {
        return hibridoDAO;
    }

    public ILoteDAO lotes() {
        return loteDAO;
    }

    public IOrdenDAO ordenes() {
        return ordenDAO;
    }
}
