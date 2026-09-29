package src.controlador;

import src.dao.OrdenDAO;
import src.modelo.OrdenPreventa;

public class GestorPreventa {
    private OrdenDAO ordenDAO = new OrdenDAO();

    public boolean crearPreventa(int idProductor, int idLote, int cantidad) {
        // Regla de negocio básica: validar cantidad
        if (cantidad <= 0) {
            System.out.println("Error: La cantidad debe ser mayor a 0.");
            return false;
        }

        OrdenPreventa nuevaOrden = new OrdenPreventa(idProductor, "RESERVADO");
        return ordenDAO.guardarOrden(nuevaOrden);
    }
}