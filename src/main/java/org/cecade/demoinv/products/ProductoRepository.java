package org.cecade.demoinv.products;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoRepository {

    private static final String DATA_FILE = "data/productos.json";
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private List<Producto> productos;

    public ProductoRepository() {
        cargar();
    }

    private void cargar() {
        Path path = Paths.get(DATA_FILE);
        if (Files.exists(path)) {
            try (Reader reader = new BufferedReader(new InputStreamReader(
                    Files.newInputStream(path), StandardCharsets.UTF_8))) {
                Type listType = new TypeToken<List<Producto>>() {}.getType();
                productos = gson.fromJson(reader, listType);
                if (productos == null) productos = new ArrayList<>();
                int maxId = productos.stream().mapToInt(Producto::getId).max().orElse(0);
                Producto.syncIdGenerator(maxId);
            } catch (Exception e) {
                e.printStackTrace();
                productos = new ArrayList<>();
            }
        } else {
            productos = new ArrayList<>();
        }
    }

    // cargarDatosPrecargados() eliminado para iniciar con inventario vacío.

    public void guardar() {
        try {
            Path path = Paths.get(DATA_FILE);
            Files.createDirectories(path.getParent());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    Files.newOutputStream(path), StandardCharsets.UTF_8))) {
                gson.toJson(productos, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Producto> getAll() {
        return new ArrayList<>(productos);
    }

    public void agregar(Producto p) {
        productos.add(p);
        guardar();
    }

    public void actualizar(Producto p) {
        for (int i = 0; i < productos.size(); i++) {
            if (productos.get(i).getId() == p.getId()) {
                productos.set(i, p);
                break;
            }
        }
        guardar();
    }

    public boolean agregarStock(int idProducto, int cantidad) {
        if (cantidad <= 0) return false;
        for (Producto p : productos) {
            if (p.getId() == idProducto) {
                p.setStock(p.getStock() + cantidad);
                guardar();
                return true;
            }
        }
        return false;
    }

    public List<Producto> topVendidos(int n) {
        return productos.stream()
                .sorted((a, b) -> Integer.compare(b.getVentasUltimos30Dias(), a.getVentasUltimos30Dias()))
                .limit(n)
                .toList();
    }

    public List<Producto> mayorStock(int n) {
        return productos.stream()
                .sorted((a, b) -> Integer.compare(b.getStock(), a.getStock()))
                .limit(n)
                .toList();
    }

    public List<Producto> buscar(String texto) {
        String lower = texto.toLowerCase();
        return productos.stream()
                .filter(p -> p.getNombre().toLowerCase().contains(lower))
                .toList();
    }

    public boolean existeProducto(String nombre) {
        String normalizado = nombre.trim().toLowerCase();
        return productos.stream()
                .anyMatch(p -> p.getNombre().trim().toLowerCase().equals(normalizado));
    }
}





