package src.controlador;

import java.util.ArrayList;
import java.util.List;

/** Resultado de importar la lista de precios: cuántas filas se aplicaron y qué filas se rechazaron y por qué. */
public class ResultadoImportacion {

    private int actualizados;
    private final List<String> rechazos = new ArrayList<>();

    void sumarActualizado() {
        actualizados++;
    }

    void rechazar(int numeroLinea, String motivo) {
        rechazos.add("Línea " + numeroLinea + ": " + motivo);
    }

    public int getActualizados() {
        return actualizados;
    }

    public List<String> getRechazos() {
        return rechazos;
    }
}
