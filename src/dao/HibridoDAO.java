package src.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;

/** Persistencia JDBC de la tabla hibridos_nidera (catálogo). */
public class HibridoDAO implements IHibridoDAO {

    static HibridoNidera mapear(ResultSet rs) throws SQLException, SeminareException {
        return HibridoNidera.crear(rs.getInt("hibrido_id"), rs.getString("codigo_variedad"),
                rs.getString("cultivo"), rs.getString("tratamiento"), rs.getBigDecimal("precio_usd"));
    }

    @Override
    public List<HibridoNidera> listar() throws SeminareException {
        String sql = "SELECT hibrido_id, codigo_variedad, cultivo, tratamiento, precio_usd "
                + "FROM hibridos_nidera ORDER BY cultivo, codigo_variedad";
        List<HibridoNidera> lista = new ArrayList<>();
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al listar el catálogo de híbridos");
        }
    }

    @Override
    public HibridoNidera buscarPorId(int id) throws SeminareException {
        String sql = "SELECT hibrido_id, codigo_variedad, cultivo, tratamiento, precio_usd "
                + "FROM hibridos_nidera WHERE hibrido_id = ?";
        try (Connection c = ConexionDB.obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al buscar el híbrido");
        }
    }
}
