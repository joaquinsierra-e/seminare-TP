package src.modelo;

import src.excepciones.DatosInvalidosException;

/**
 * Clase abstracta base para las personas/organizaciones que operan con Seminare.
 * Aplica ABSTRACCIÓN (no se puede instanciar) y ENCAPSULAMIENTO (atributos privados).
 */
public abstract class Persona {

    private int id;
    private String nombre;

    protected Persona(int id, String nombre) throws DatosInvalidosException {
        this.id = id;
        setNombre(nombre);
    }

    /** Cada subclase describe qué tipo de persona es (método abstracto → polimorfismo). */
    public abstract String getRol();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public final void setNombre(String nombre) throws DatosInvalidosException {
        if (nombre == null || nombre.isBlank()) {
            throw new DatosInvalidosException("El nombre / razón social no puede estar vacío.");
        }
        this.nombre = nombre.trim();
    }
}
