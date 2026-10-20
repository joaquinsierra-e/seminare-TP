package src.controlador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import src.dao.IOrdenDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.EstadoOrden;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;
import src.util.Algoritmos;

/**
 * Clase de CONTROL del CU03 "Despacho y Logística". Las órdenes RESERVADAS esperan en una COLA (FIFO):
 * se despachan en el orden en que se registraron.
 */
public class GestorDespacho {

    private final IOrdenDAO ordenDAO;
    private final Queue<OrdenPreventa> cola = new LinkedList<>();

    public GestorDespacho(IOrdenDAO ordenDAO) {
        this.ordenDAO = ordenDAO;
    }

    /** Reconstruye la cola desde la fuente de datos, ordenada por fecha y número de orden. */
    private void refrescarCola() throws SeminareException {
        List<OrdenPreventa> pendientes = new ArrayList<>(ordenDAO.listarPorEstado(EstadoOrden.RESERVADO));
        Algoritmos.ordenarPorSeleccion(pendientes, (a, b) -> {
            int cmp = a.getFecha().compareTo(b.getFecha());
            return cmp != 0 ? cmp : Integer.compare(a.getId(), b.getId());
        });
        cola.clear();
        cola.addAll(pendientes);
    }

    public List<OrdenPreventa> verCola() throws SeminareException {
        refrescarCola();
        return new ArrayList<>(cola);
    }

    /** Despacha la orden que está primera en la cola y emite el remito. */
    public OrdenDespacho despacharSiguiente(String patenteCamion) throws SeminareException {
        refrescarCola();
        OrdenPreventa orden = cola.peek();
        if (orden == null) {
            throw new DatosInvalidosException("No hay órdenes pendientes de despacho.");
        }
        String remito = String.format("R-0001-%08d", orden.getId());
        OrdenDespacho despacho = new OrdenDespacho(0, orden, LocalDateTime.now(), remito, patenteCamion); // valida patente
        orden.marcarDespachada(); // regla de dominio
        ordenDAO.registrarDespacho(despacho);
        cola.poll();
        return despacho;
    }
}
