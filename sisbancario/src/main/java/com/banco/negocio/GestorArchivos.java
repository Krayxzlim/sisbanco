package com.banco.negocio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GestorArchivos {
    private static final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .setPrettyPrinting()
        .create();

    public static <T> List<T> cargarLista(String archivo, Type tipoLista) {
        try (Reader reader = new FileReader(archivo)) {
            return gson.fromJson(reader, tipoLista);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public static <T> void guardarLista(String archivo, List<T> lista) {
        try (FileWriter writer = new FileWriter(archivo, false)) {
            gson.toJson(lista, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error guardando archivo: " + archivo + "\n" + e.getMessage());
        }
    }

}