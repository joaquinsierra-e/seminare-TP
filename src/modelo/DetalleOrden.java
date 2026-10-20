package src.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Línea de una orden de preventa: un lote, una cantidad y el precio unitario pactado (USD). */
public class DetalleOrden {

    private int id;
    private final LoteInsumo lote;
    private final int cantidad;
    private final BigDecimal precioUnitarioUsd;

    public DetalleOrden(int id, LoteInsumo lote, int cantidad, BigDecimal precioUnitarioUsd) {
        this.id = id;
        this.lote = lote;
        this.cantidad = cantidad;
        this.precioUnitarioUsd = precioUnitarioUsd;
    }

    public BigDecimal subtotalUsd() {
        return precioUnitarioUsd.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LoteInsumo getLote() {
        return lote;
    }

    public int getCantidad() {
        return cantidad;
    }

    public BigDecimal getPrecioUnitarioUsd() {
        return precioUnitarioUsd;
    }

    @Override
    public String toString() {
        return String.format("   - %s x%d @ USD %s = USD %s (lote %s)",
                lote.getHibrido().getCodigoVariedad(), cantidad, precioUnitarioUsd, subtotalUsd(), lote.getNumeroLote());
    }
}
