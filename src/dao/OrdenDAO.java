package src.dao;

import src.modelo.OrdenPreventa;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class OrdenDAO {
    private final String URL = "jdbc:mysql://localhost:3306/seminare_db";
    private final String USER = "root";
    private final String PASS = "root"; // Tu contraseña de MySQL

    public boolean guardarOrden(OrdenPreventa orden) {
        String sql = "INSERT INTO ordenes_preventa (productor_id, fecha_emision, estado) VALUES (?, NOW(), ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, orden.getProductorId());
            stmt.setString(2, orden.getEstado());
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}