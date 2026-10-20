package src.controlador;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import src.dao.ILoteDAO;
import src.dao.IOrdenDAO;
import src.dao.IProductorDAO;
import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.modelo.CondicionPago;
import src.modelo.DetalleOrden;
import src.modelo.EstadoOrden;
import src.modelo.LoteInsumo;
import src.modelo.OrdenPreventa;
import src.modelo.Productor;

/**
 * Clase de CONTROL del CU01 "Registrar Preventa": aplica las reglas de negocio
 * (productor existente, stock suficiente por lote, cotización en dólares) y coordina a los DAO.
 * Mantiene una PILA (LIFO) con las preventas de la sesión para poder cancelar la última.
 */
public class GestorPreventa {

    private final IProductorDAO productorDAO;
    private final ILoteDAO loteDAO;
    private final IOrdenDAO ordenDAO;
    private final Deque<OrdenPreventa> historialSesion = new ArrayDeque<>();

    public GestorPreventa(IProductorDAO productorDAO, ILoteDAO loteDAO, IOrdenDAO ordenDAO) {
        this.productorDAO = productorDAO;
        this.loteDAO = loteDAO;
        this.ordenDAO = ordenDAO;
    }

    public List<Productor> listarProductores() throws SeminareException {
        return productorDAO.listar();
    }

    public Productor registrarProductor(String cuit, String razonSocial, String localidad, String telefono)
            throws SeminareException {
        Productor p = new Productor(0, cuit, razonSocial, localidad, telefono); // valida CUIT y nombre
        productorDAO.insertar(p);
        return p;
    }

    /** Paso 1 del CU01: abre una orden en memoria (todavía no se persiste). */
    public OrdenPreventa iniciarOrden(int idProductor, CondicionPago condicion, BigDecimal cotizacionUsd)
            throws SeminareException {
        Productor productor = productorDAO.buscarPorId(idProductor);
        if (productor == null) {
            throw new DatosInvalidosException("No existe el productor #" + idProductor + ".");
        }
        return new OrdenPreventa(productor, condicion, cotizacionUsd);
    }

    /** Paso 2: agrega un lote a la orden. Lanza StockInsuficienteException si no alcanzan las bolsas (CP-02). */
    public DetalleOrden agregarItem(OrdenPreventa orden, int idLote, int cantidad) throws SeminareException {
        LoteInsumo lote = loteDAO.buscarPorId(idLote);
        if (lote == null) {
            throw new DatosInvalidosException("No existe el lote #" + idLote + ".");
        }
        return orden.agregarDetalle(lote, cantidad);
    }

    /** Paso 3: persiste la orden y descuenta el stock en una transacción (CP-01). */
    public OrdenPreventa confirmar(OrdenPreventa orden) throws SeminareException {
        if (orden.getDetalles().isEmpty()) {
            throw new DatosInvalidosException("La orden no tiene ítems; agregue al menos un lote.");
        }
        ordenDAO.registrarReserva(orden);
        historialSesion.push(orden);
        return orden;
    }

    /** Cancela la última preventa registrada en esta sesión (desapila) y devuelve el stock. */
    public OrdenPreventa cancelarUltimaPreventa() throws SeminareException {
        OrdenPreventa ultima = historialSesion.peek();
        if (ultima == null) {
            throw new DatosInvalidosException("No hay preventas registradas en esta sesión para cancelar.");
        }
        OrdenPreventa actual = ordenDAO.buscarPorId(ultima.getId()); // estado fresco desde la fuente de datos
        if (actual == null) {
            throw new DatosInvalidosException("La orden #" + ultima.getId() + " ya no existe.");
        }
        actual.cancelar(); // regla de dominio: solo se cancelan las RESERVADAS
        ordenDAO.cancelar(actual);
        historialSesion.pop();
        return actual;
    }

    public int cantidadEnHistorial() {
        return historialSesion.size();
    }

    public List<OrdenPreventa> listarPreventas(EstadoOrden estado) throws SeminareException {
        return ordenDAO.listarPorEstado(estado);
    }
}
