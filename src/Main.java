package src;

import src.controlador.GestorPreventa;

public class Main {
    public static void main(String[] args) {
        GestorPreventa gestor = new GestorPreventa();
        
        // Simulación del CU01: Registrar Preventa
        boolean resultado = gestor.crearPreventa(1, 1, 20);
        
        if (resultado) {
            System.out.println("¡Orden de Preventa registrada con éxito en MySQL!");
        } else {
            System.out.println("Error al registrar la orden.");
        }
    }
}