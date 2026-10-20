package src.modelo;

import java.math.BigDecimal;

import src.excepciones.DatosInvalidosException;

/** Variedad de soja. Bonificación: 2% desde 100 bolsas. */
public class HibridoSoja extends HibridoNidera {

    public HibridoSoja(int id, String codigoVariedad, String tratamiento, BigDecimal precioUsd)
            throws DatosInvalidosException {
        super(id, codigoVariedad, "Soja", tratamiento, precioUsd);
    }

    @Override
    public String getPresentacion() {
        return "bolsa de 40 kg";
    }

    @Override
    protected BigDecimal porcentajeBonificacion(int cantidadBolsas) {
        return cantidadBolsas >= 100 ? BigDecimal.valueOf(2) : BigDecimal.ZERO;
    }
}
