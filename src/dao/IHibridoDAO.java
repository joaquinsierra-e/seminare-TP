package src.dao;

import java.math.BigDecimal;
import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;

/** Contrato de acceso al catálogo de híbridos Nidera. */
public interface IHibridoDAO {
    List<HibridoNidera> listar() throws SeminareException;

    /** @return el híbrido o {@code null} si no existe. */
    HibridoNidera buscarPorId(int id) throws SeminareException;

    /** Busca por código de variedad (ej. "AX 7784 VT3P"), sin distinguir mayúsculas. @return null si no existe. */
    HibridoNidera buscarPorCodigo(String codigoVariedad) throws SeminareException;

    /** Actualiza el precio de lista en USD. @return true si se modificó una fila. */
    boolean actualizarPrecio(int id, BigDecimal precioUsd) throws SeminareException;
}
