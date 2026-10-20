package src.util;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/** Algoritmos de ordenación y búsqueda implementados a mano (sin Collections.sort) con fines didácticos. */
public final class Algoritmos {

    private Algoritmos() {
    }

    /** Ordenación por INSERCIÓN, estable, O(n²) en el peor caso. Modifica la lista recibida. */
    public static <T> void ordenarPorInsercion(List<T> lista, Comparator<? super T> criterio) {
        for (int i = 1; i < lista.size(); i++) {
            T actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && criterio.compare(lista.get(j), actual) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }

    /** Ordenación por SELECCIÓN, O(n²). Modifica la lista recibida. */
    public static <T> void ordenarPorSeleccion(List<T> lista, Comparator<? super T> criterio) {
        for (int i = 0; i < lista.size() - 1; i++) {
            int menor = i;
            for (int j = i + 1; j < lista.size(); j++) {
                if (criterio.compare(lista.get(j), lista.get(menor)) < 0) {
                    menor = j;
                }
            }
            if (menor != i) {
                T temporal = lista.get(i);
                lista.set(i, lista.get(menor));
                lista.set(menor, temporal);
            }
        }
    }

    /** Búsqueda BINARIA, O(log n). La lista debe estar ordenada ascendentemente por la clave. @return índice o -1. */
    public static <T, K extends Comparable<K>> int busquedaBinaria(List<T> ordenada, K clave, Function<T, K> extractor) {
        int bajo = 0;
        int alto = ordenada.size() - 1;
        while (bajo <= alto) {
            int medio = (bajo + alto) >>> 1;
            int cmp = extractor.apply(ordenada.get(medio)).compareTo(clave);
            if (cmp == 0) {
                return medio;
            } else if (cmp < 0) {
                bajo = medio + 1;
            } else {
                alto = medio - 1;
            }
        }
        return -1;
    }

    /** Búsqueda LINEAL, O(n); no requiere lista ordenada. @return índice o -1. */
    public static <T> int busquedaLineal(List<T> lista, java.util.function.Predicate<T> condicion) {
        for (int i = 0; i < lista.size(); i++) {
            if (condicion.test(lista.get(i))) {
                return i;
            }
        }
        return -1;
    }
}
