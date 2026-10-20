package src.util;

import java.math.BigDecimal;
import java.util.Scanner;

/** Lectura robusta desde teclado: repite la pregunta hasta recibir un valor válido (manejo de excepciones). */
public class Consola {

    private final Scanner scanner = new Scanner(System.in);

    public String leerTexto(String mensaje) {
        System.out.print(mensaje);
        if (!scanner.hasNextLine()) {
            throw new EntradaCerradaException();
        }
        return scanner.nextLine().trim();
    }

    public String leerTextoNoVacio(String mensaje) {
        String valor = leerTexto(mensaje);
        while (valor.isEmpty()) {
            System.out.println("  ! El valor no puede estar vacío.");
            valor = leerTexto(mensaje);
        }
        return valor;
    }

    public int leerEntero(String mensaje, int minimo, int maximo) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("  ! Ingrese un número entre " + minimo + " y " + maximo + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + texto + "' no es un número entero válido.");
            }
        }
    }

    public BigDecimal leerDecimal(String mensaje, BigDecimal minimoExclusivo) {
        while (true) {
            String texto = leerTexto(mensaje).replace(',', '.');
            try {
                BigDecimal valor = new BigDecimal(texto);
                if (valor.compareTo(minimoExclusivo) > 0) {
                    return valor;
                }
                System.out.println("  ! El valor debe ser mayor a " + minimoExclusivo + ".");
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + texto + "' no es un número válido.");
            }
        }
    }

    public boolean confirmar(String mensaje) {
        while (true) {
            String r = leerTexto(mensaje + " (s/n): ").toLowerCase();
            if (r.equals("s") || r.equals("si") || r.equals("sí")) {
                return true;
            }
            if (r.equals("n") || r.equals("no")) {
                return false;
            }
            System.out.println("  ! Responda s o n.");
        }
    }
}
