package org.cecade.demoinv.inventory;

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

public class IngresoRepository {
    private static final String DATA_FILE = "data/ingresos.json";
    private final Gson gson;
    private List<Ingreso> ingresos;

    public IngresoRepository() {
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
                Type listType = new TypeToken<List<Ingreso>>() {}.getType();
                ingresos = gson.fromJson(reader, listType);
                if (ingresos == null) ingresos = new ArrayList<>();
            } catch (Exception e) {
                e.printStackTrace();
                ingresos = new ArrayList<>();
            }
        } else {
            ingresos = new ArrayList<>();
        }
    }

    public void guardar() {
        try {
            Path path = Paths.get(DATA_FILE);
            Files.createDirectories(path.getParent());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(path), StandardCharsets.UTF_8))) {
                gson.toJson(ingresos, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void agregar(Ingreso ingreso) {
        ingresos.add(ingreso);
        guardar();
    }

    public List<Ingreso> getAll() {
        return new ArrayList<>(ingresos);
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

