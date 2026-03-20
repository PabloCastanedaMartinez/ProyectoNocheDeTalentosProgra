package org.cecade.demoinv.reports;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.cecade.demoinv.inventory.Ingreso;
import org.cecade.demoinv.inventory.IngresoRepository;
import org.cecade.demoinv.products.Producto;
import org.cecade.demoinv.products.ProductoRepository;
import org.cecade.demoinv.sales.Venta;
import org.cecade.demoinv.sales.VentaRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryReportService {

    private final VentaRepository ventaRepository;
    private final IngresoRepository ingresoRepository;
    private final ProductoRepository productoRepository;

    public InventoryReportService(VentaRepository ventaRepository) {
        this(ventaRepository, new IngresoRepository(), new ProductoRepository());
    }

    public InventoryReportService(VentaRepository ventaRepository, IngresoRepository ingresoRepository) {
        this(ventaRepository, ingresoRepository, new ProductoRepository());
    }

    public InventoryReportService(VentaRepository ventaRepository, IngresoRepository ingresoRepository, ProductoRepository productoRepository) {
        this.ventaRepository = ventaRepository;
        this.ingresoRepository = ingresoRepository;
        this.productoRepository = productoRepository;
    }

    public File generarReporte(PeriodoReporte periodo) throws IOException, DocumentException {
        // Filtrar datos
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = switch (periodo) {
            case DIARIO -> now.toLocalDate().atStartOfDay();
            case SEMANAL -> now.minusWeeks(1);
            case MENSUAL -> now.minusMonths(1);
            case TRIMESTRAL -> now.minusMonths(3);
            case SEMESTRAL -> now.minusMonths(6);
            case ANUAL -> now.minusYears(1);
            default -> now.minusDays(1);
        };

        List<Venta> ventas = ventaRepository.getAll().stream()
                .filter(v -> v.getFecha().isAfter(start) && v.getFecha().isBefore(now.plusSeconds(1)))
                .toList();

        List<Ingreso> ingresos = ingresoRepository.getAll().stream()
                .filter(i -> i.getFecha().isAfter(start) && i.getFecha().isBefore(now.plusSeconds(1)))
                .toList();

        // Obtener inventario actual
        List<Producto> productos = productoRepository.getAll();

        // Calcular totales
        BigDecimal totalVendido = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCostoVentas = ventas.stream()
                .map(v -> v.getCostoTotal() != null ? v.getCostoTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGanancia = totalVendido.subtract(totalCostoVentas);

        BigDecimal totalValorInventario = productos.stream()
                .map(p -> p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalTransacciones = ventas.size();
        int totalIngresos = ingresos.size();
        int totalItemsVendidos = ventas.stream().mapToInt(Venta::getCantidad).sum();
        int totalItemsIngresados = ingresos.stream().mapToInt(Ingreso::getCantidad).sum();
        int totalStock = productos.stream().mapToInt(Producto::getStock).sum();

        // Configurar PDF
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(now);
        String filename = "Reporte_Flujo_Inventario_" + periodo.name() + "_" + timestamp + ".pdf";
        Path reportDir = Paths.get("reports/sales");
        if (!Files.exists(reportDir)) {
            Files.createDirectories(reportDir);
        }
        File pdfFile = reportDir.resolve(filename).toFile();

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

        document.open();

        // Encabezado
        Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph header = new Paragraph("Reporte de Flujo de Inventario - " + periodo.getDescripcion(), fontHeader);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);

        document.add(new Paragraph(" ")); // Spacer

        Font fontSub = FontFactory.getFont(FontFactory.HELVETICA, 12);
        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);

        document.add(new Paragraph("Fecha de generación: " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")), fontSub));
        document.add(new Paragraph("Periodo: " + start.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontSub));
        document.add(new Paragraph("Total Vendido: Q " + totalVendido.toPlainString(), fontSub));
        document.add(new Paragraph("Ganancia Total Estimada: Q " + totalGanancia.toPlainString(), fontSub));
        document.add(new Paragraph("Valor Total del Inventario: Q " + totalValorInventario.toPlainString(), fontSub));
        document.add(new Paragraph("Stock Total de Productos: " + totalStock, fontSub));
        document.add(new Paragraph("Transacciones de Ventas: " + totalTransacciones + " (" + totalItemsVendidos + " items)", fontSub));
        document.add(new Paragraph("Registros de Ingresos: " + totalIngresos + " (" + totalItemsIngresados + " items)", fontSub));

        document.add(new Paragraph(" ")); // Spacer

        // === SECCIÓN VENTAS ===
        document.add(new Paragraph("Detalle de Ventas", fontBold));
        document.add(new Paragraph(" ", fontSub)); // Spacer

        // Tabla Ventas
        PdfPTable table = new PdfPTable(6); // Fecha, Producto, Cantidad, Precio, Total, Ganancia
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 4, 1.5f, 2, 2, 2});

        addTableHeader(table, "Fecha");
        addTableHeader(table, "Producto");
        addTableHeader(table, "Cant.");
        addTableHeader(table, "Precio U.");
        addTableHeader(table, "Total");
        addTableHeader(table, "Ganancia");

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

        for (Venta v : ventas) {
            table.addCell(v.getFecha().format(dateFmt));
            table.addCell(v.getProductoNombre());
            table.addCell(String.valueOf(v.getCantidad()));
            table.addCell("Q " + v.getPrecioUnitario().toPlainString());
            table.addCell("Q " + v.getTotal().toPlainString());

            BigDecimal costo = v.getCostoTotal() != null ? v.getCostoTotal() : BigDecimal.ZERO;
            BigDecimal ganancia = v.getTotal().subtract(costo);
            table.addCell("Q " + ganancia.toPlainString());
        }

        if (ventas.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No hay ventas en este periodo"));
            cell.setColspan(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        document.add(table);

        document.add(new Paragraph(" ")); // Spacer

        // === SECCIÓN INGRESOS ===
        document.add(new Paragraph("Ingresos de Stock", fontBold));
        document.add(new Paragraph(" ", fontSub)); // Spacer

        // Tabla Ingresos
        PdfPTable tableIng = new PdfPTable(3); // Fecha, Producto, Cantidad
        tableIng.setWidthPercentage(100);
        tableIng.setWidths(new float[]{3, 5, 2});

        addTableHeader(tableIng, "Fecha");
        addTableHeader(tableIng, "Producto");
        addTableHeader(tableIng, "Cant. Agregada");

        for (Ingreso i : ingresos) {
            tableIng.addCell(i.getFecha().format(dateFmt));
            tableIng.addCell(i.getProductoNombre());
            tableIng.addCell(String.valueOf(i.getCantidad()));
        }

        if (ingresos.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No hay ingresos de stock en este periodo"));
            cell.setColspan(3);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableIng.addCell(cell);
        }

        document.add(tableIng);

        document.add(new Paragraph(" ")); // Spacer

        // === SECCIÓN INVENTARIO ACTUAL ===
        document.add(new Paragraph("Inventario Actual", fontBold));
        document.add(new Paragraph(" ", fontSub)); // Spacer

        // Tabla Inventario
        PdfPTable tableInv = new PdfPTable(4); // Producto, Stock, Precio U., Valor Total
        tableInv.setWidthPercentage(100);
        tableInv.setWidths(new float[]{4, 1.5f, 2, 2.5f});

        addTableHeader(tableInv, "Producto");
        addTableHeader(tableInv, "Stock");
        addTableHeader(tableInv, "Precio U.");
        addTableHeader(tableInv, "Valor Total");

        for (Producto p : productos) {
            tableInv.addCell(p.getNombre());
            tableInv.addCell(String.valueOf(p.getStock()));
            tableInv.addCell("Q " + p.getPrecioUnitario().toPlainString());
            BigDecimal valorTotal = p.getPrecioUnitario().multiply(BigDecimal.valueOf(p.getStock()));
            tableInv.addCell("Q " + valorTotal.toPlainString());
        }

        if (productos.isEmpty()) {
            PdfPCell cell = new PdfPCell(new Phrase("No hay productos en inventario"));
            cell.setColspan(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tableInv.addCell(cell);
        }

        document.add(tableInv);

        document.close();

        return pdfFile;
    }

    private void addTableHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        header.setPhrase(new Phrase(title));
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }
}
