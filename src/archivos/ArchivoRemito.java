package src.archivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import src.excepciones.ArchivoException;
import src.modelo.DetalleOrden;
import src.modelo.OrdenDespacho;
import src.modelo.OrdenPreventa;

/**
 * Guarda cada remito de despacho como archivo de texto en la carpeta remitos/ y permite volver a leerlo.
 * El remito es el comprobante que viaja con el camión; tenerlo en un archivo permite imprimirlo o enviarlo.
 */
public class ArchivoRemito {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final File carpeta;

    public ArchivoRemito() {
        this(new File("remitos"));
    }

    public ArchivoRemito(File carpeta) {
        this.carpeta = carpeta;
    }

    /** Escribe el remito y devuelve el archivo creado. */
    public File guardar(OrdenDespacho despacho) throws ArchivoException {
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new ArchivoException("No se pudo crear la carpeta " + carpeta.getAbsolutePath());
        }
        File archivo = new File(carpeta, despacho.getRemito() + ".txt");
        OrdenPreventa orden = despacho.getOrden();
        try (PrintWriter salida = new PrintWriter(new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8)))) {
            salida.println("SEMINARE S.R.L. - Distribuidor oficial Nidera - Río Cuarto, Córdoba");
            salida.println("REMITO N° " + despacho.getRemito());
            salida.println("Fecha de despacho : " + despacho.getFecha().format(FMT));
            salida.println("Orden de preventa : #" + orden.getId());
            salida.println("Productor         : " + orden.getProductor().getNombre()
                    + " (CUIT " + orden.getProductor().getCuit() + ")");
            salida.println("Destino           : " + orden.getProductor().getLocalidad());
            salida.println("Camión (patente)  : " + despacho.getPatenteCamion());
            salida.println("--------------------------------------------------------------");
            salida.printf("%-18s %-22s %8s%n", "Lote", "Híbrido", "Bolsas");
            int totalBolsas = 0;
            for (DetalleOrden d : orden.getDetalles()) {
                salida.printf("%-18s %-22s %8d%n", d.getLote().getNumeroLote(),
                        d.getLote().getHibrido().getCodigoVariedad(), d.getCantidad());
                totalBolsas += d.getCantidad();
            }
            salida.println("--------------------------------------------------------------");
            salida.printf("%-41s %8d%n", "TOTAL DE BOLSAS", totalBolsas);
            // PrintWriter no lanza IOException: hay que preguntar si hubo error al escribir
            if (salida.checkError()) {
                throw new ArchivoException("Error al escribir el remito " + archivo.getName());
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo crear el archivo del remito: " + e.getMessage(), e);
        }
        return archivo;
    }

    /** Lee un remito guardado, línea por línea. */
    public List<String> leer(String nombreArchivo) throws ArchivoException {
        File archivo = new File(carpeta, nombreArchivo);
        List<String> lineas = new ArrayList<>();
        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            while ((linea = entrada.readLine()) != null) {
                lineas.add(linea);
            }
        } catch (FileNotFoundException e) {
            throw new ArchivoException("No existe el remito " + nombreArchivo + ".", e);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer el remito " + nombreArchivo + ": " + e.getMessage(), e);
        }
        return lineas;
    }

    /** Nombres de los remitos guardados, ordenados alfabéticamente (arreglo devuelto por File.list). */
    public String[] listar() {
        String[] nombres = carpeta.list((dir, nombre) -> nombre.endsWith(".txt"));
        if (nombres == null) { // la carpeta todavía no existe
            return new String[0];
        }
        Arrays.sort(nombres);
        return nombres;
    }
}
