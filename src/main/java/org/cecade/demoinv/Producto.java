package org.cecade.demoinv;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

public class Producto {
    private static final AtomicInteger ID_GEN = new AtomicInteger(0);

    private int id;
    private String nombre;
    private BigDecimal precioUnitario;
    private int stock;
    private int ventasUltimos30Dias;
    private String rutaImagenLocal;

    public Producto() {}

    public Producto(String nombre, BigDecimal precioUnitario, int stock, int ventasUltimos30Dias, String rutaImagenLocal) {
        this.id = ID_GEN.incrementAndGet();
        this.nombre = nombre;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.ventasUltimos30Dias = ventasUltimos30Dias;
        this.rutaImagenLocal = rutaImagenLocal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public int getVentasUltimos30Dias() { return ventasUltimos30Dias; }
    public void setVentasUltimos30Dias(int ventasUltimos30Dias) { this.ventasUltimos30Dias = ventasUltimos30Dias; }

    public String getRutaImagenLocal() { return rutaImagenLocal; }
    public void setRutaImagenLocal(String rutaImagenLocal) { this.rutaImagenLocal = rutaImagenLocal; }

    public static void syncIdGenerator(int maxId) {
        ID_GEN.set(maxId);
    }

    @Override
    public String toString() {
        return nombre + " (Stock: " + stock + ", Ventas: " + ventasUltimos30Dias + ")";
    }
}

