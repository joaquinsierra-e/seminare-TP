package src.dao.memoria;

import java.util.ArrayList;
import java.util.List;

import src.dao.IOrdenDAO;
import src.excepciones.SeminareException;
import src.excepciones.StockInsuficienteException;
import src.modelo.DetalleOrden;
import src.modelo.EstadoOrden;
import src.modelo.LoteInsumo;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;

/** DAO de órdenes sobre {@link AlmacenMemoria}; reproduce la atomicidad y los controles del DAO JDBC. */
public class OrdenDAOMemoria implements IOrdenDAO {

    private final AlmacenMemoria almacen;

    public OrdenDAOMemoria(AlmacenMemoria almacen) {
        this.almacen = almacen;
    }

    private LoteInsumo loteReal(int id) {
        for (LoteInsumo l : almacen.lotes) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    @Override
    public void registrarReserva(OrdenPreventa orden) throws SeminareException {
        // 1) verificar todo antes de modificar nada (equivale al rollback de la transacción)
        for (DetalleOrden d : orden.getDetalles()) {
            LoteInsumo real = loteReal(d.getLote().getId());
            if (real == null || real.getBolsasDisponibles() < d.getCantidad()) {
                throw new StockInsuficienteException("Stock Insuficiente en el lote " + d.getLote().getNumeroLote()
                        + ": otro usuario lo modificó recientemente.");
            }
        }
        // 2) aplicar
        for (DetalleOrden d : orden.getDetalles()) {
            loteReal(d.getLote().getId()).reservar(d.getCantidad());
        }
        orden.setId(almacen.nuevoIdOrden());
        almacen.ordenes.add(orden);
    }

    @Override
    public void cancelar(OrdenPreventa orden) throws SeminareException {
        for (DetalleOrden d : orden.getDetalles()) {
            loteReal(d.getLote().getId()).liberar(d.getCantidad());
        }
    }

    @Override
    public void registrarDespacho(OrdenDespacho despacho) {
        despacho.setId(almacen.nuevoIdDespacho());
        almacen.despachos.add(despacho);
    }

    @Override
    public List<OrdenPreventa> listarPorEstado(EstadoOrden estado) {
        List<OrdenPreventa> lista = new ArrayList<>();
        for (OrdenPreventa o : almacen.ordenes) {
            if (o.getEstado() == estado) {
                lista.add(o);
            }
        }
        return lista;
    }

    @Override
    public OrdenPreventa buscarPorId(int id) {
        for (OrdenPreventa o : almacen.ordenes) {
            if (o.getId() == id) {
                return o;
            }
        }
        return null;
    }
}
