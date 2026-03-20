package org.cecade.demoinv.sales;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentaRepository {
    private static final String DATA_FILE = "data/ventas.json";
    private final Gson gson;
    private List<Venta> ventas;

    public VentaRepository() {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        cargar();
    }

    private void cargar() {
        Path path = Paths.get(DATA_FILE);
        if (Files.exists(path)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(path), StandardCharsets.UTF_8))) {
                Type listType = new TypeToken<List<Venta>>() {}.getType();
                ventas = gson.fromJson(reader, listType);
                if (ventas == null) ventas = new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                ventas = new ArrayList<>();
            }
        } else {
            ventas = new ArrayList<>();
        }
    }

    public void guardar() {
        try {
            Path path = Paths.get(DATA_FILE);
            Files.createDirectories(path.getParent());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(path), StandardCharsets.UTF_8))) {
                gson.toJson(ventas, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void agregar(Venta venta) {
        ventas.add(venta);
        guardar();
    }

    public List<Venta> getAll() {
        return new ArrayList<>(ventas);
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(FORMATTER.format(src));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}

