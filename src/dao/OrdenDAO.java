package src.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import src.excepciones.DatosInvalidosException;
import src.excepciones.SeminareException;
import src.excepciones.StockInsuficienteException;
import src.modelo.CondicionPago;
import src.modelo.DetalleOrden;
import src.modelo.EstadoOrden;
import src.modelo.LoteInsumo;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;
import src.modelo.Productor;

/**
 * Persistencia JDBC de ordenes_preventa, detalles_orden y ordenes_despacho.
 * Todas las escrituras usan PreparedStatement y una transacción (commit / rollback).
 */
public class OrdenDAO implements IOrdenDAO {

    private static final String SQL_INSERT_ORDEN =
            "INSERT INTO ordenes_preventa (productor_id, fecha_emision, estado, condicion_pago, cotizacion_usd) "
                    + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_DETALLE =
            "INSERT INTO detalles_orden (orden_id, lote_id, cantidad_bolsas, precio_unitario_usd) VALUES (?, ?, ?, ?)";
    // La condición "bolsas_disponibles >= ?" hace atómico el control de stock aun con usuarios concurrentes.
    private static final String SQL_DESCONTAR_STOCK =
            "UPDATE lotes_insumo SET bolsas_disponibles = bolsas_disponibles - ? "
                    + "WHERE lote_id = ? AND bolsas_disponibles >= ?";
    private static final String SQL_DEVOLVER_STOCK =
            "UPDATE lotes_insumo SET bolsas_disponibles = bolsas_disponibles + ? WHERE lote_id = ?";
    private static final String SQL_CAMBIAR_ESTADO =
            "UPDATE ordenes_preventa SET estado = ? WHERE orden_id = ? AND estado = 'RESERVADO'";
    private static final String SQL_INSERT_DESPACHO =
            "INSERT INTO ordenes_despacho (orden_id, fecha_despacho, remito, patente_camion) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ORDEN =
            "SELECT o.orden_id, o.fecha_emision, o.estado, o.condicion_pago, o.cotizacion_usd, "
                    + "p.productor_id, p.razon_social, p.cuit, p.telefono, p.localidad "
                    + "FROM ordenes_preventa o JOIN productores p ON p.productor_id = o.productor_id ";
    private static final String SQL_SELECT_DETALLES =
            "SELECT d.detalle_id, d.cantidad_bolsas, d.precio_unitario_usd, " + LoteDAO.COLUMNAS
                    + " FROM detalles_orden d JOIN lotes_insumo l ON l.lote_id = d.lote_id" + LoteDAO.JOIN_HIBRIDO
                    + " WHERE d.orden_id = ? ORDER BY d.detalle_id";

    @Override
    public void registrarReserva(OrdenPreventa orden) throws SeminareException {
        try (Connection c = ConexionDB.obtener()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_ORDEN, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, orden.getProductor().getId());
                    ps.setTimestamp(2, Timestamp.valueOf(orden.getFecha()));
                    ps.setString(3, orden.getEstado().name());
                    ps.setString(4, orden.getCondicionPago().name());
                    ps.setBigDecimal(5, orden.getCotizacionUsd());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            orden.setId(keys.getInt(1));
                        }
                    }
                }
                for (DetalleOrden d : orden.getDetalles()) {
                    try (PreparedStatement ps = c.prepareStatement(SQL_DESCONTAR_STOCK)) {
                        ps.setInt(1, d.getCantidad());
                        ps.setInt(2, d.getLote().getId());
                        ps.setInt(3, d.getCantidad());
                        if (ps.executeUpdate() == 0) {
                            throw new StockInsuficienteException("Stock Insuficiente en el lote "
                                    + d.getLote().getNumeroLote() + ": otro usuario lo modificó recientemente.");
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_DETALLE, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, orden.getId());
                        ps.setInt(2, d.getLote().getId());
                        ps.setInt(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitarioUsd());
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) {
                                d.setId(keys.getInt(1));
                            }
                        }
                    }
                }
                c.commit();
            } catch (SQLException e) {
                revertir(c);
                throw ConexionDB.traducir(e, "No se pudo registrar la preventa");
            } catch (SeminareException e) {
                revertir(c);
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error de conexión al registrar la preventa");
        }
    }

    @Override
    public void cancelar(OrdenPreventa orden) throws SeminareException {
        try (Connection c = ConexionDB.obtener()) {
            c.setAutoCommit(false);
            try {
                cambiarEstado(c, orden.getId(), EstadoOrden.CANCELADO);
                for (DetalleOrden d : orden.getDetalles()) {
                    try (PreparedStatement ps = c.prepareStatement(SQL_DEVOLVER_STOCK)) {
                        ps.setInt(1, d.getCantidad());
                        ps.setInt(2, d.getLote().getId());
                        ps.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                revertir(c);
                throw ConexionDB.traducir(e, "No se pudo cancelar la preventa");
            } catch (SeminareException e) {
                revertir(c);
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error de conexión al cancelar la preventa");
        }
    }

    @Override
    public void registrarDespacho(OrdenDespacho despacho) throws SeminareException {
        try (Connection c = ConexionDB.obtener()) {
            c.setAutoCommit(false);
            try {
                cambiarEstado(c, despacho.getOrden().getId(), EstadoOrden.DESPACHADO);
                try (PreparedStatement ps = c.prepareStatement(SQL_INSERT_DESPACHO, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, despacho.getOrden().getId());
                    ps.setTimestamp(2, Timestamp.valueOf(despacho.getFecha()));
                    ps.setString(3, despacho.getRemito());
                    ps.setString(4, despacho.getPatenteCamion());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            despacho.setId(keys.getInt(1));
                        }
                    }
                }
                c.commit();
            } catch (SQLException e) {
                revertir(c);
                throw ConexionDB.traducir(e, "No se pudo registrar el despacho");
            } catch (SeminareException e) {
                revertir(c);
                throw e;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error de conexión al registrar el despacho");
        }
    }

    @Override
    public List<OrdenPreventa> listarPorEstado(EstadoOrden estado) throws SeminareException {
        List<OrdenPreventa> lista = new ArrayList<>();
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ORDEN + "WHERE o.estado = ? ORDER BY o.orden_id")) {
            ps.setString(1, estado.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearCabecera(rs));
                }
            }
            for (OrdenPreventa o : lista) {
                cargarDetalles(c, o);
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al listar órdenes");
        }
    }

    @Override
    public OrdenPreventa buscarPorId(int id) throws SeminareException {
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(SQL_SELECT_ORDEN + "WHERE o.orden_id = ?")) {
            ps.setInt(1, id);
            OrdenPreventa orden = null;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    orden = mapearCabecera(rs);
                }
            }
            if (orden != null) {
                cargarDetalles(c, orden);
            }
            return orden;
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al buscar la orden");
        }
    }

    // ---------- utilitarios internos ----------

    private OrdenPreventa mapearCabecera(ResultSet rs) throws SQLException, SeminareException {
        Productor productor = ProductorDAO.mapear(rs);
        BigDecimal cotizacion = rs.getBigDecimal("cotizacion_usd");
        return new OrdenPreventa(rs.getInt("orden_id"), productor, rs.getTimestamp("fecha_emision").toLocalDateTime(),
                CondicionPago.valueOf(rs.getString("condicion_pago")), EstadoOrden.valueOf(rs.getString("estado")),
                cotizacion);
    }

    private void cargarDetalles(Connection c, OrdenPreventa orden) throws SQLException, SeminareException {
        try (PreparedStatement ps = c.prepareStatement(SQL_SELECT_DETALLES)) {
            ps.setInt(1, orden.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LoteInsumo lote = LoteDAO.mapear(rs);
                    orden.incorporarDetalle(new DetalleOrden(rs.getInt("detalle_id"), lote,
                            rs.getInt("cantidad_bolsas"), rs.getBigDecimal("precio_unitario_usd")));
                }
            }
        }
    }

    /** Cambia el estado solo si la orden sigue RESERVADA (evita doble despacho / doble cancelación). */
    private void cambiarEstado(Connection c, int idOrden, EstadoOrden nuevo) throws SQLException, DatosInvalidosException {
        try (PreparedStatement ps = c.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            ps.setString(1, nuevo.name());
            ps.setInt(2, idOrden);
            if (ps.executeUpdate() == 0) {
                throw new DatosInvalidosException("La orden #" + idOrden + " ya no está en estado RESERVADO.");
            }
        }
    }

    private void revertir(Connection c) {
        try {
            c.rollback();
        } catch (SQLException ignorada) {
            // si el rollback falla la conexión se cierra igual y MySQL descarta la transacción abierta
        }
    }
}
