package src.modelo;

import src.excepciones.DatosInvalidosException;
import src.excepciones.StockInsuficienteException;

/**
 * Lote físico de un híbrido en el depósito de la Ruta A005 (tabla lotes_insumo, RF-02).
 * ENCAPSULA el stock: solo se modifica mediante reservar() y liberar().
 */
public class LoteInsumo {

    private int id;
    private final String numeroLote;
    private final HibridoNidera hibrido;
    private int bolsasDisponibles;
    private final double poderGerminativo; // porcentaje 0-100

    public LoteInsumo(int id, String numeroLote, HibridoNidera hibrido, int bolsasDisponibles,
                      double poderGerminativo) throws DatosInvalidosException {
        if (numeroLote == null || numeroLote.isBlank()) {
            throw new DatosInvalidosException("El número de lote es obligatorio.");
        }
        if (hibrido == null) {
            throw new DatosInvalidosException("El lote debe pertenecer a un híbrido del catálogo.");
        }
        if (bolsasDisponibles < 0) {
            throw new DatosInvalidosException("Las bolsas disponibles no pueden ser negativas.");
        }
        if (poderGerminativo < 0 || poderGerminativo > 100) {
            throw new DatosInvalidosException("El poder germinativo debe estar entre 0 y 100.");
        }
        this.id = id;
        this.numeroLote = numeroLote.trim();
        this.hibrido = hibrido;
        this.bolsasDisponibles = bolsasDisponibles;
        this.poderGerminativo = poderGerminativo;
    }

    /** Descuenta {@code cantidad} bolsas del stock al registrar una preventa (CP-01 / CP-02). */
    public void reservar(int cantidad) throws StockInsuficienteException, DatosInvalidosException {
        validarCantidad(cantidad);
        if (cantidad > bolsasDisponibles) {
            throw new StockInsuficienteException(numeroLote, bolsasDisponibles, cantidad);
        }
        bolsasDisponibles -= cantidad;
    }

    /** Devuelve {@code cantidad} bolsas al stock (cancelación de una preventa). */
    public void liberar(int cantidad) throws DatosInvalidosException {
        validarCantidad(cantidad);
        bolsasDisponibles += cantidad;
    }

    private void validarCantidad(int cantidad) throws DatosInvalidosException {
        if (cantidad <= 0) {
            throw new DatosInvalidosException("La cantidad de bolsas debe ser mayor a cero.");
        }
    }

    /** Copia independiente (la usa la implementación en memoria para simular lecturas de la BD). */
    public LoteInsumo copiar() {
        try {
            return new LoteInsumo(id, numeroLote, hibrido, bolsasDisponibles, poderGerminativo);
        } catch (DatosInvalidosException e) {
            throw new IllegalStateException("Un lote válido no puede copiarse como inválido", e);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public HibridoNidera getHibrido() {
        return hibrido;
    }

    public int getBolsasDisponibles() {
        return bolsasDisponibles;
    }

    public double getPoderGerminativo() {
        return poderGerminativo;
    }

    @Override
    public String toString() {
        return String.format("[%d] Lote %s | %s %s | disponibles=%d | PG %.2f%%",
                id, numeroLote, hibrido.getCultivo(), hibrido.getCodigoVariedad(), bolsasDisponibles, poderGerminativo);
    }
}
