package src.modelo;

import java.util.Date;

public class OrdenPreventa {
    private int ordenId;
    private int productorId;
    private Date fechaEmision;
    private String estado;

    public OrdenPreventa(int productorId, String estado) {
        this.productorId = productorId;
        this.fechaEmision = new Date();
        this.estado = estado;
    }

    // Getters y Setters
    public int getOrdenId() { return ordenId; }
    public void setOrdenId(int ordenId) { this.ordenId = ordenId; }
    public int getProductorId() { return productorId; }
    public void setProductorId(int productorId) { this.productorId = productorId; }
    public Date getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(Date fechaEmision) { this.fechaEmision = fechaEmision; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}