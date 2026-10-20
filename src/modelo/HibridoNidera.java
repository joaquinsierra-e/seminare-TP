package src.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import src.excepciones.DatosInvalidosException;

/**
 * Híbrido / variedad del catálogo Nidera (tabla hibridos_nidera, RF-01).
 *
 * Clase ABSTRACTA: no existe un "híbrido genérico", sino uno de maíz, soja o girasol.
 * Cada subclase define su presentación comercial y su política de bonificación por volumen.
 * El controlador trabaja siempre con el tipo abstracto (POLIMORFISMO).
 */
public abstract class HibridoNidera {

    private int id;
    private final String codigoVariedad;
    private final String cultivo;
    private final String tratamiento;
    private final BigDecimal precioUsd;

    protected HibridoNidera(int id, String codigoVariedad, String cultivo, String tratamiento, BigDecimal precioUsd)
            throws DatosInvalidosException {
        if (codigoVariedad == null || codigoVariedad.isBlank()) {
            throw new DatosInvalidosException("El código de variedad es obligatorio.");
        }
        if (precioUsd == null || precioUsd.signum() <= 0) {
            throw new DatosInvalidosException("El precio en USD debe ser mayor a cero.");
        }
        this.id = id;
        this.codigoVariedad = codigoVariedad.trim();
        this.cultivo = cultivo;
        this.tratamiento = tratamiento == null ? "" : tratamiento.trim();
        this.precioUsd = precioUsd.setScale(2, RoundingMode.HALF_UP);
    }

    /** Fábrica: elige la subclase concreta según el cultivo almacenado en la base de datos. */
    public static HibridoNidera crear(int id, String codigoVariedad, String cultivo, String tratamiento,
                                      BigDecimal precioUsd) throws DatosInvalidosException {
        String c = cultivo == null ? "" : cultivo.trim().toLowerCase();
        switch (c) {
            case "maíz":
            case "maiz":
                return new HibridoMaiz(id, codigoVariedad, tratamiento, precioUsd);
            case "soja":
                return new HibridoSoja(id, codigoVariedad, tratamiento, precioUsd);
            case "girasol":
                return new HibridoGirasol(id, codigoVariedad, tratamiento, precioUsd);
            default:
                throw new DatosInvalidosException("Cultivo no soportado por el prototipo: '" + cultivo + "'.");
        }
    }

    /** Presentación comercial de la bolsa (varía por cultivo). */
    public abstract String getPresentacion();

    /** Porcentaje de bonificación (0-100) según la cantidad de bolsas; cada cultivo define su política. */
    protected abstract BigDecimal porcentajeBonificacion(int cantidadBolsas);

    /** Precio unitario en USD aplicando la bonificación por volumen (método plantilla). */
    public BigDecimal precioUnitarioPara(int cantidadBolsas) {
        BigDecimal descuento = porcentajeBonificacion(cantidadBolsas).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
        return precioUsd.multiply(BigDecimal.ONE.subtract(descuento)).setScale(2, RoundingMode.HALF_UP);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoVariedad() {
        return codigoVariedad;
    }

    public String getCultivo() {
        return cultivo;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public BigDecimal getPrecioUsd() {
        return precioUsd;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s | %s | %s | USD %s/bolsa", id, cultivo, codigoVariedad, tratamiento,
                getPresentacion(), precioUsd);
    }
}
