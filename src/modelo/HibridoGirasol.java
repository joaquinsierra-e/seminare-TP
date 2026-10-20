package src.modelo;

import java.math.BigDecimal;

import src.excepciones.DatosInvalidosException;

/** Híbrido de girasol. Sin bonificación por volumen (precio de lista). */
public class HibridoGirasol extends HibridoNidera {

    public HibridoGirasol(int id, String codigoVariedad, String tratamiento, BigDecimal precioUsd)
            throws DatosInvalidosException {
        super(id, codigoVariedad, "Girasol", tratamiento, precioUsd);
    }

    @Override
    public String getPresentacion() {
        return "bolsa de 150.000 semillas";
    }

    @Override
    protected BigDecimal porcentajeBonificacion(int cantidadBolsas) {
        return BigDecimal.ZERO;
    }
}
