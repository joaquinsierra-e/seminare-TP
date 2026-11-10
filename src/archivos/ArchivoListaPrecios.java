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
import java.util.ArrayList;
import java.util.List;

import src.excepciones.ArchivoException;
import src.modelo.HibridoNidera;

/**
 * Lista de precios de Nidera en formato CSV separado por punto y coma:
 *     codigo_variedad;cultivo;precio_usd
 * Se exporta para que el área comercial la edite en Excel y se vuelve a importar para actualizar MySQL.
 */
public class ArchivoListaPrecios {

    public static final String SEPARADOR = ";";
    public static final String[] ENCABEZADO = {"codigo_variedad", "cultivo", "precio_usd"};

    /** Escribe el catálogo actual en el archivo indicado. */
    public void exportar(List<HibridoNidera> catalogo, File archivo) throws ArchivoException {
        File carpeta = archivo.getAbsoluteFile().getParentFile();
        if (carpeta != null && !carpeta.exists() && !carpeta.mkdirs()) {
            throw new ArchivoException("No se pudo crear la carpeta " + carpeta);
        }
        try (PrintWriter salida = new PrintWriter(new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8)))) {
            salida.println(String.join(SEPARADOR, ENCABEZADO));
            for (HibridoNidera h : catalogo) {
                salida.println(h.getCodigoVariedad() + SEPARADOR + h.getCultivo() + SEPARADOR + h.getPrecioUsd());
            }
            if (salida.checkError()) {
                throw new ArchivoException("Error al escribir " + archivo.getName());
            }
        } catch (IOException e) {
            throw new ArchivoException("No se pudo exportar la lista de precios: " + e.getMessage(), e);
        }
    }

    /**
     * Lee el CSV y devuelve una fila por línea; cada fila es un ARREGLO con los campos separados.
     * Saltea el encabezado y las líneas vacías. No valida el contenido: eso lo hace el controlador.
     */
    public List<String[]> leerFilas(File archivo) throws ArchivoException {
        List<String[]> filas = new ArrayList<>();
        try (BufferedReader entrada = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8))) {
            String linea;
            boolean primera = true;
            while ((linea = entrada.readLine()) != null) {
                if (primera) {
                    primera = false;
                    linea = linea.replace("﻿", ""); // BOM que agrega Excel al guardar en UTF-8
                    if (linea.toLowerCase().startsWith(ENCABEZADO[0])) {
                        continue;
                    }
                }
                if (!linea.isBlank()) {
                    filas.add(linea.split(SEPARADOR, -1));
                }
            }
        } catch (FileNotFoundException e) {
            throw new ArchivoException("No se encontró el archivo " + archivo.getPath() + ".", e);
        } catch (IOException e) {
            throw new ArchivoException("No se pudo leer " + archivo.getPath() + ": " + e.getMessage(), e);
        }
        return filas;
    }
}
