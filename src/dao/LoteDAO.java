package src.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;
import src.modelo.LoteInsumo;

/** Persistencia JDBC de la tabla lotes_insumo (con JOIN al catálogo). */
public class LoteDAO implements ILoteDAO {

    /** Columnas de lote + híbrido; reutilizadas por OrdenDAO al reconstruir los detalles. */
    static final String COLUMNAS = "l.lote_id, l.numero_lote, l.bolsas_disponibles, l.poder_germinativo, "
            + "h.hibrido_id, h.codigo_variedad, h.cultivo, h.tratamiento, h.precio_usd";
    static final String JOIN_HIBRIDO = " JOIN hibridos_nidera h ON h.hibrido_id = l.hibrido_id";

    static LoteInsumo mapear(ResultSet rs) throws SQLException, SeminareException {
        HibridoNidera hibrido = HibridoDAO.mapear(rs);
        return new LoteInsumo(rs.getInt("lote_id"), rs.getString("numero_lote"), hibrido,
                rs.getInt("bolsas_disponibles"), rs.getDouble("poder_germinativo"));
    }

    @Override
    public List<LoteInsumo> listar() throws SeminareException {
        String sql = "SELECT " + COLUMNAS + " FROM lotes_insumo l" + JOIN_HIBRIDO + " ORDER BY l.lote_id";
        List<LoteInsumo> lista = new ArrayList<>();
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al listar lotes");
        }
    }

    @Override
    public LoteInsumo buscarPorId(int id) throws SeminareException {
        String sql = "SELECT " + COLUMNAS + " FROM lotes_insumo l" + JOIN_HIBRIDO + " WHERE l.lote_id = ?";
        try (Connection c = ConexionDB.obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al buscar el lote");
        }
    }

    @Override
    public void insertar(LoteInsumo lote) throws SeminareException {
        String sql = "INSERT INTO lotes_insumo (hibrido_id, numero_lote, bolsas_disponibles, poder_germinativo) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lote.getHibrido().getId());
            ps.setString(2, lote.getNumeroLote());
            ps.setInt(3, lote.getBolsasDisponibles());
            ps.setDouble(4, lote.getPoderGerminativo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    lote.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "No se pudo registrar el lote (¿número de lote ya existente?)");
        }
    }
}
