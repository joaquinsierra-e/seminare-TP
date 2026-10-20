package src.dao;

import java.util.List;

import src.excepciones.SeminareException;
import src.modelo.HibridoNidera;

/** Contrato de acceso al catálogo de híbridos Nidera. */
public interface IHibridoDAO {
    List<HibridoNidera> listar() throws SeminareException;

    /** @return el híbrido o {@code null} si no existe. */
    HibridoNidera buscarPorId(int id) throws SeminareException;
}
