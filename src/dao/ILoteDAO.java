package src.dao;

import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.LoteInsumo;

/** Contrato de persistencia de lotes de insumos. */
public interface ILoteDAO {
    List<LoteInsumo> listar() throws SeminareException;

    /** @return el lote (copia fresca de la fuente de datos) o {@code null} si no existe. */
    LoteInsumo buscarPorId(int id) throws SeminareException;

    /** Inserta y completa el id generado. Lanza DatosInvalidosException si el número de lote ya existe. */
    void insertar(LoteInsumo lote) throws SeminareException;
}
