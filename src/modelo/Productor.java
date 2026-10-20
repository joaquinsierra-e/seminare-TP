package src.modelo;

import src.excepciones.DatosInvalidosException;

/** Productor agrícola (cliente de Seminare). Hereda de {@link Persona}. */
public class Productor extends Persona {

    private String cuit;
    private String localidad;
    private String telefono;

    public Productor(int id, String cuit, String razonSocial, String localidad, String telefono)
            throws DatosInvalidosException {
        super(id, razonSocial);
        setCuit(cuit);
        this.localidad = localidad == null ? "" : localidad.trim();
        this.telefono = telefono == null ? "" : telefono.trim();
    }

    @Override
    public String getRol() {
        return "Productor Agrícola";
    }

    public String getCuit() {
        return cuit;
    }

    /** Valida el formato XX-XXXXXXXX-X y el dígito verificador (módulo 11). */
    public final void setCuit(String cuit) throws DatosInvalidosException {
        if (!esCuitValido(cuit)) {
            throw new DatosInvalidosException("CUIT inválido: '" + cuit + "'. Formato esperado XX-XXXXXXXX-X con dígito verificador correcto.");
        }
        this.cuit = cuit;
    }

    public static boolean esCuitValido(String cuit) {
        if (cuit == null || !cuit.matches("\\d{2}-\\d{8}-\\d")) {
            return false;
        }
        String digitos = cuit.replace("-", "");
        int[] pesos = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};
        int suma = 0;
        for (int i = 0; i < pesos.length; i++) {
            suma += Character.getNumericValue(digitos.charAt(i)) * pesos[i];
        }
        int verificador = 11 - (suma % 11);
        if (verificador == 11) {
            verificador = 0;
        }
        return verificador != 10 && verificador == Character.getNumericValue(digitos.charAt(10));
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s | CUIT %s | %s", getId(), getNombre(), cuit, localidad);
    }
}
