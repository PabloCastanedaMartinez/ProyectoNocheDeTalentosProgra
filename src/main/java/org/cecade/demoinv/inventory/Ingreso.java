package org.cecade.demoinv.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ingreso {
    private String id;
    private LocalDateTime fecha;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal costoUnitario;
    private int cantidadDisponible;

    public Ingreso() {
    }

    public Ingreso(String id, LocalDateTime fecha, int productoId, String productoNombre, int cantidad, BigDecimal costoUnitario) {
        this.id = id;
        this.fecha = fecha;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario;
        this.cantidadDisponible = cantidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public int getCantidadDisponible() {
        return cantidadDisponible;
    }

    public void setCantidadDisponible(int cantidadDisponible) {
        this.cantidadDisponible = cantidadDisponible;
    }
}
