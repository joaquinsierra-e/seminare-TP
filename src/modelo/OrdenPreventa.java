package src.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import src.excepciones.DatosInvalidosException;
import src.excepciones.StockInsuficienteException;

/**
 * Orden de preventa (entidad raíz del CU01). Agrupa DetalleOrden y conoce su ciclo de vida:
 * RESERVADO → DESPACHADO | CANCELADO.
 */
public class OrdenPreventa {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private final Productor productor;
    private final LocalDateTime fecha;
    private final CondicionPago condicionPago;
    private final BigDecimal cotizacionUsd; // pesos por dólar
    private EstadoOrden estado;
    private final List<DetalleOrden> detalles = new ArrayList<>();

    /** Constructor para una orden NUEVA (id lo asigna la BD, estado inicial RESERVADO). */
    public OrdenPreventa(Productor productor, CondicionPago condicionPago, BigDecimal cotizacionUsd)
            throws DatosInvalidosException {
        this(0, productor, LocalDateTime.now(), condicionPago, EstadoOrden.RESERVADO, cotizacionUsd);
    }

    /** Constructor para rehidratar una orden existente desde la persistencia. */
    public OrdenPreventa(int id, Productor productor, LocalDateTime fecha, CondicionPago condicionPago,
                         EstadoOrden estado, BigDecimal cotizacionUsd) throws DatosInvalidosException {
        if (productor == null) {
            throw new DatosInvalidosException("La orden requiere un productor.");
        }
        if (condicionPago == null) {
            throw new DatosInvalidosException("La orden requiere una condición de pago.");
        }
        if (cotizacionUsd == null || cotizacionUsd.signum() <= 0) {
            throw new DatosInvalidosException("La cotización del dólar debe ser mayor a cero.");
        }
        this.id = id;
        this.productor = productor;
        this.fecha = fecha;
        this.condicionPago = condicionPago;
        this.estado = estado;
        this.cotizacionUsd = cotizacionUsd;
    }

    /** Agrega una línea reservando el stock del lote. Falla con StockInsuficienteException si no alcanza. */
    public DetalleOrden agregarDetalle(LoteInsumo lote, int cantidad)
            throws StockInsuficienteException, DatosInvalidosException {
        if (estado != EstadoOrden.RESERVADO) {
            throw new DatosInvalidosException("Solo se pueden agregar ítems a una orden en estado RESERVADO.");
        }
        for (DetalleOrden d : detalles) {
            if (d.getLote().getId() == lote.getId()) {
                throw new DatosInvalidosException("El lote " + lote.getNumeroLote() + " ya fue agregado a esta orden.");
            }
        }
        lote.reservar(cantidad); // valida stock; si falla, no se agrega nada
        BigDecimal precio = lote.getHibrido().precioUnitarioPara(cantidad); // polimorfismo
        DetalleOrden detalle = new DetalleOrden(0, lote, cantidad, precio);
        detalles.add(detalle);
        return detalle;
    }

    /** Incorpora una línea ya persistida (no toca el stock). */
    public void incorporarDetalle(DetalleOrden detalle) {
        detalles.add(detalle);
    }

    public void marcarDespachada() throws DatosInvalidosException {
        exigirReservada("despachar");
        estado = EstadoOrden.DESPACHADO;
    }

    public void cancelar() throws DatosInvalidosException {
        exigirReservada("cancelar");
        for (DetalleOrden d : detalles) {
            d.getLote().liberar(d.getCantidad());
        }
        estado = EstadoOrden.CANCELADO;
    }

    private void exigirReservada(String accion) throws DatosInvalidosException {
        if (estado != EstadoOrden.RESERVADO) {
            throw new DatosInvalidosException("No se puede " + accion + " la orden #" + id
                    + ": está en estado " + estado + ".");
        }
    }

    public BigDecimal totalUsd() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleOrden d : detalles) {
            total = total.add(d.subtotalUsd());
        }
        return total;
    }

    public BigDecimal totalArs() {
        return totalUsd().multiply(cotizacionUsd).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Productor getProductor() {
        return productor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public CondicionPago getCondicionPago() {
        return condicionPago;
    }

    public BigDecimal getCotizacionUsd() {
        return cotizacionUsd;
    }

    public EstadoOrden getEstado() {
        return estado;
    }

    public List<DetalleOrden> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String numero = id == 0 ? "(sin confirmar)" : "#" + id;
        sb.append(String.format("Orden %s | %s | %s | %s | %s%n", numero, fecha.format(FMT), estado,
                productor.getNombre(), condicionPago.getDescripcion()));
        for (DetalleOrden d : detalles) {
            sb.append(d).append(System.lineSeparator());
        }
        sb.append(String.format("   TOTAL: USD %s  (ARS %s @ %s)", totalUsd(), totalArs(), cotizacionUsd));
        return sb.toString();
    }
}
