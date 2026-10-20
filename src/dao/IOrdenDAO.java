package src.dao;

import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.EstadoOrden;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;

/** Contrato de persistencia de órdenes de preventa y despachos. Cada operación de escritura es atómica. */
public interface IOrdenDAO {

    /** Guarda la orden y descuenta el stock de cada lote en una única transacción (CU01). */
    void registrarReserva(OrdenPreventa orden) throws SeminareException;

    /** Marca la orden como CANCELADO y devuelve las bolsas a cada lote en una única transacción. */
    void cancelar(OrdenPreventa orden) throws SeminareException;

    /** Marca la orden como DESPACHADO y registra el remito en una única transacción (CU03). */
    void registrarDespacho(OrdenDespacho despacho) throws SeminareException;

    /** Órdenes con el estado indicado, ordenadas por número. */
    List<OrdenPreventa> listarPorEstado(EstadoOrden estado) throws SeminareException;

    /** @return la orden con sus detalles o {@code null} si no existe. */
    OrdenPreventa buscarPorId(int id) throws SeminareException;
}
