package src.dao;

import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.Productor;

/** Contrato de persistencia de productores (abstracción: el controlador no conoce JDBC). */
public interface IProductorDAO {
    List<Productor> listar() throws SeminareException;

    /** @return el productor o {@code null} si no existe. */
    Productor buscarPorId(int id) throws SeminareException;

    /** Inserta y completa el id generado. Lanza DatosInvalidosException si el CUIT ya existe. */
    void insertar(Productor productor) throws SeminareException;
}
