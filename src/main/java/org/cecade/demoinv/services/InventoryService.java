package org.cecade.demoinv.services;

import org.cecade.demoinv.products.Producto;
import org.cecade.demoinv.products.ProductoRepository;
import org.cecade.demoinv.reports.PeriodoReporte;
import org.cecade.demoinv.reports.InventoryReportService;
import org.cecade.demoinv.sales.Venta;
import org.cecade.demoinv.sales.VentaRepository;
import org.cecade.demoinv.inventory.Ingreso;
import org.cecade.demoinv.inventory.IngresoRepository;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InventoryService {

    private final ProductoRepository productRepo;
    private final VentaRepository saleRepo;
    private final IngresoRepository ingresoRepo;
    private final InventoryReportService reportService;

    public InventoryService() {
        this.productRepo = new ProductoRepository();
        this.saleRepo = new VentaRepository();
        this.ingresoRepo = new IngresoRepository();
        this.reportService = new InventoryReportService(saleRepo, ingresoRepo, productRepo);
    }

    public List<Producto> getTopSelling(int limit) {
        return productRepo.topVendidos(limit);
    }

    public List<Producto> getTopStock(int limit) {
        return productRepo.mayorStock(limit);
    }

    public List<Producto> getAllProducts() {
        return productRepo.getAll();
    }

    public List<Producto> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        return productRepo.buscar(query);
    }

    public Optional<Producto> getProductById(int id) {
        return productRepo.getAll().stream()
                .filter(p -> p.getId() == id)
                .findFirst();
    }

    public void sellProduct(int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }

        Producto product = getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        if (quantity > product.getStock()) {
            throw new IllegalStateException("Stock insuficiente. Disponible: " + product.getStock());
        }

        // PEPS / FIFO Logic
        List<Ingreso> batches = ingresoRepo.getAll().stream()
                .filter(i -> i.getProductoId() == productId && i.getCantidadDisponible() > 0)
                .sorted((i1, i2) -> i1.getFecha().compareTo(i2.getFecha()))
                .toList();

        BigDecimal totalCost = BigDecimal.ZERO;
        int remainingToSell = quantity;

        for (Ingreso batch : batches) {
            if (remainingToSell <= 0) break;

            int available = batch.getCantidadDisponible();
            int toTake = Math.min(available, remainingToSell);

            // Calculate cost for this portion
            BigDecimal batchCost = batch.getCostoUnitario() != null
                    ? batch.getCostoUnitario()
                    : BigDecimal.ZERO; // Handle legacy data

            totalCost = totalCost.add(batchCost.multiply(BigDecimal.valueOf(toTake)));

            // Update batch
            batch.setCantidadDisponible(available - toTake);
            remainingToSell -= toTake;
        }

        // Save batch updates
        ingresoRepo.guardar();

        // Update product state
        product.setStock(product.getStock() - quantity);
        product.setVentasUltimos30Dias(product.getVentasUltimos30Dias() + quantity);
        productRepo.actualizar(product);

        // Record sale
        BigDecimal totalSale = product.getPrecioUnitario().multiply(BigDecimal.valueOf(quantity));
        Venta sale = new Venta(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                product.getId(),
                product.getNombre(),
                quantity,
                product.getPrecioUnitario(),
                totalSale,
                totalCost
        );
        saleRepo.agregar(sale);
    }

    public void addStock(int productId, int quantity, BigDecimal unitCost) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que 0.");
        }
        if (unitCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo no puede ser negativo.");
        }

        Producto product = getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        boolean success = productRepo.agregarStock(productId, quantity);
        if (!success) {
            throw new IllegalArgumentException("No se pudo actualizar el stock del producto.");
        }

        // Registrar ingreso
        Ingreso ingreso = new Ingreso(
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                product.getId(),
                product.getNombre(),
                quantity,
                unitCost
        );
        ingresoRepo.agregar(ingreso);
    }

    public void createProduct(String name, BigDecimal price, int initialStock, int sales30Days, String imagePath, BigDecimal initialCost) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío.");
        }
        if (productRepo.existeProducto(name)) {
            throw new IllegalArgumentException("Ya existe un producto con el nombre \"" + name + "\".");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        }
        if (initialStock < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (initialCost.compareTo(BigDecimal.ZERO) < 0) {
             throw new IllegalArgumentException("El costo no puede ser negativo.");
        }
        if (sales30Days < 0) {
            sales30Days = 0;
        }
        if (imagePath == null || imagePath.isBlank()) {
            imagePath = "images/placeholder.png";
        }

        Producto newProduct = new Producto(name.trim(), price, initialStock, sales30Days, imagePath);
        productRepo.agregar(newProduct);

        // Register initial stock as the first batch
        if (initialStock > 0) {
            Ingreso initialBatch = new Ingreso(
                    UUID.randomUUID().toString(),
                    LocalDateTime.now(),
                    newProduct.getId(),
                    newProduct.getNombre(),
                    initialStock,
                    initialCost
            );
            ingresoRepo.agregar(initialBatch);
        }
    }

    public File generateReport(PeriodoReporte periodo) throws Exception {
        return reportService.generarReporte(periodo);
    }

    public void updatePrice(int id, BigDecimal nuevoPrecio, boolean enPromocion, int diasPromocion) {
        if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        }

        if (enPromocion && diasPromocion <= 0) {
            throw new IllegalArgumentException("La duración de la promoción debe ser mayor a 0 días.");
        }

        Producto product = getProductById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado."));

        product.setPrecioUnitario(nuevoPrecio);
        product.setEnPromocion(enPromocion);
        if (enPromocion) {
            product.setFinPromocion(LocalDateTime.now().plusDays(diasPromocion));
        } else {
            product.setFinPromocion(null);
        }

        productRepo.actualizar(product);
    }
}
