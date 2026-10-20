package src.modelo;

import java.math.BigDecimal;

import src.excepciones.DatosInvalidosException;

/** Híbrido de maíz. Bonificación: 3% desde 50 bolsas y 5% desde 100 bolsas. */
public class HibridoMaiz extends HibridoNidera {

    public HibridoMaiz(int id, String codigoVariedad, String tratamiento, BigDecimal precioUsd)
            throws DatosInvalidosException {
        super(id, codigoVariedad, "Maíz", tratamiento, precioUsd);
    }

    @Override
    public String getPresentacion() {
        return "bolsa de 60.000 semillas";
    }

    @Override
    protected BigDecimal porcentajeBonificacion(int cantidadBolsas) {
        if (cantidadBolsas >= 100) {
            return BigDecimal.valueOf(5);
        }
        return cantidadBolsas >= 50 ? BigDecimal.valueOf(3) : BigDecimal.ZERO;
    }
}
