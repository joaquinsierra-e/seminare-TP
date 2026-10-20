package src.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import src.excepciones.DatosInvalidosException;

/** Registro de la salida física de una orden desde el depósito (CU03): remito + camión. */
public class OrdenDespacho {

    private int id;
    private final OrdenPreventa orden;
    private final LocalDateTime fecha;
    private final String remito;
    private final String patenteCamion;

    public OrdenDespacho(int id, OrdenPreventa orden, LocalDateTime fecha, String remito, String patenteCamion)
            throws DatosInvalidosException {
        if (orden == null) {
            throw new DatosInvalidosException("El despacho requiere una orden.");
        }
        if (patenteCamion == null || !patenteCamion.trim().toUpperCase().matches("[A-Z]{3}\\d{3}|[A-Z]{2}\\d{3}[A-Z]{2}")) {
            throw new DatosInvalidosException("Patente inválida. Formatos aceptados: ABC123 o AB123CD.");
        }
        this.id = id;
        this.orden = orden;
        this.fecha = fecha;
        this.remito = remito;
        this.patenteCamion = patenteCamion.trim().toUpperCase();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public OrdenPreventa getOrden() {
        return orden;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getRemito() {
        return remito;
    }

    public String getPatenteCamion() {
        return patenteCamion;
    }

    @Override
    public String toString() {
        return String.format("Remito %s | Orden #%d | Camión %s | %s",
                remito, orden.getId(), patenteCamion,
                fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }
}
