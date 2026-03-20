package org.cecade.demoinv.sales;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {
    private String id;
    private LocalDateTime fecha;
    private int productoId;
    private String productoNombre;
    private int cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal totalVenta;
    private BigDecimal costoTotal;

    public Venta() {
    }

    public Venta(String id, LocalDateTime fecha, int productoId, String productoNombre, int cantidad, BigDecimal precioUnitario, BigDecimal totalVenta, BigDecimal costoTotal) {
        this.id = id;
        this.fecha = fecha;
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.totalVenta = totalVenta;
        this.costoTotal = costoTotal;
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

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public BigDecimal getTotal() {
        return totalVenta;
    }

    public void setTotal(BigDecimal totalVenta) {
        this.totalVenta = totalVenta;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }
}
