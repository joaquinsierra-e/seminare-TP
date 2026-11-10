package src.vista;

import src.excepciones.ArchivoException;
import src.excepciones.DatosInvalidosException;
import src.excepciones.PersistenciaException;
import src.excepciones.SeminareException;
import src.excepciones.StockInsuficienteException;
import src.util.Consola;

/**
 * VISTA base del patrón MVC. Clase ABSTRACTA que reúne lo que todas las pantallas repiten:
 * dibujar el menú, leer la opción y mostrar los errores sin que el programa se cierre.
 * Cada vista concreta solo define su título, sus opciones y qué hacer con cada una (método plantilla).
 */
public abstract class VistaBase {

    /** Acción de menú que puede fallar con una excepción controlada del sistema. */
    protected interface Accion {
        void ejecutar() throws SeminareException;
    }

    protected final Consola consola;

    protected VistaBase(Consola consola) {
        this.consola = consola;
    }

    protected abstract String titulo();

    /** Opciones del menú en un arreglo: la posición 0 se muestra como opción 1, y así sucesivamente. */
    protected abstract String[] opciones();

    /** Ejecuta la opción elegida (1..opciones().length). */
    protected abstract void atender(int opcion) throws SeminareException;

    protected String textoOpcionCero() {
        return "Volver";
    }

    /** Muestra el menú hasta que el usuario elige 0. */
    public final void mostrar() {
        int opcion;
        do {
            String[] ops = opciones();
            System.out.println();
            System.out.println(titulo());
            for (int i = 0; i < ops.length; i++) {
                System.out.println((i + 1) + ". " + ops[i]);
            }
            System.out.println("0. " + textoOpcionCero());
            opcion = consola.leerEntero("Opción: ", 0, ops.length);
            final int elegida = opcion;
            if (elegida != 0) {
                ejecutar(() -> atender(elegida));
            }
        } while (opcion != 0);
    }

    /**
     * Manejo centralizado de excepciones: el usuario ve un mensaje claro según el tipo de error
     * y vuelve al menú. Las subclases van primero y SeminareException al final (si no, no compila).
     */
    protected final void ejecutar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (StockInsuficienteException e) {
            System.out.println("  [STOCK] " + e.getMessage());
        } catch (DatosInvalidosException e) {
            System.out.println("  [DATOS] " + e.getMessage());
        } catch (PersistenciaException e) {
            System.out.println("  [BASE DE DATOS] " + e.getMessage());
        } catch (ArchivoException e) {
            System.out.println("  [ARCHIVO] " + e.getMessage());
        } catch (SeminareException e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }
}
