package src.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.Productor;

/** Persistencia JDBC de la tabla productores. */
public class ProductorDAO implements IProductorDAO {

    static final String COLUMNAS = "productor_id, razon_social, cuit, telefono, localidad";

    static Productor mapear(ResultSet rs) throws SQLException, SeminareException {
        return new Productor(rs.getInt("productor_id"), rs.getString("cuit"), rs.getString("razon_social"),
                rs.getString("localidad"), rs.getString("telefono"));
    }

    @Override
    public List<Productor> listar() throws SeminareException {
        String sql = "SELECT " + COLUMNAS + " FROM productores ORDER BY razon_social";
        List<Productor> lista = new ArrayList<>();
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al listar productores");
        }
    }

    @Override
    public Productor buscarPorId(int id) throws SeminareException {
        String sql = "SELECT " + COLUMNAS + " FROM productores WHERE productor_id = ?";
        try (Connection c = ConexionDB.obtener(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "Error al buscar el productor");
        }
    }

    @Override
    public void insertar(Productor p) throws SeminareException {
        String sql = "INSERT INTO productores (razon_social, cuit, telefono, localidad) VALUES (?, ?, ?, ?)";
        try (Connection c = ConexionDB.obtener();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setString(2, p.getCuit());
            ps.setString(3, p.getTelefono());
            ps.setString(4, p.getLocalidad());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ConexionDB.traducir(e, "No se pudo registrar el productor (¿CUIT ya existente?)");
        }
    }
}
