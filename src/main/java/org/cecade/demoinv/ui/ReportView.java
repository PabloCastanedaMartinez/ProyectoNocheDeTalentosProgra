package org.cecade.demoinv.ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.cecade.demoinv.reports.PeriodoReporte;
import org.cecade.demoinv.services.InventoryService;

import java.awt.Desktop;
import java.io.File;

public class ReportView extends VBox {

    private final InventoryService service;
    private final TextArea statusArea;
    private final ComboBox<PeriodoReporte> comboPeriodo;

    public ReportView(InventoryService service) {
        this.service = service;

        this.setSpacing(20);
        this.getStyleClass().add("main-panel");
        this.setPadding(new javafx.geometry.Insets(30));
        this.setAlignment(Pos.TOP_CENTER);
        this.setMaxWidth(600);

        Label titulo = new Label("Generación de Reportes");
        titulo.getStyleClass().add("ficha-titulo");

        // Period Selection
        HBox periodBox = new HBox(15);
        periodBox.setAlignment(Pos.CENTER);

        Label lblPeriodo = new Label("Seleccionar Periodo:");
        lblPeriodo.getStyleClass().add("form-label");

        comboPeriodo = new ComboBox<>();
        comboPeriodo.getItems().addAll(PeriodoReporte.values());
        comboPeriodo.setValue(PeriodoReporte.MENSUAL);
        comboPeriodo.setPrefWidth(200);

        periodBox.getChildren().addAll(lblPeriodo, comboPeriodo);

        // Buttons
        HBox btnBox = new HBox(20);
        btnBox.setAlignment(Pos.CENTER);

        Button btnGenerar = new Button("Generar PDF");
        btnGenerar.getStyleClass().add("btn-ingresar");
        btnGenerar.setStyle("-fx-font-size: 14px; -fx-padding: 10 25;");
        btnGenerar.setOnAction(e -> generateReport());

        Button btnAbrirCarpeta = new Button("Abrir Carpeta");
        btnAbrirCarpeta.getStyleClass().add("btn-buscar");
        btnAbrirCarpeta.setStyle("-fx-font-size: 14px; -fx-padding: 10 25;");
        btnAbrirCarpeta.setOnAction(e -> openFolder());

        btnBox.getChildren().addAll(btnGenerar, btnAbrirCarpeta);

        // Status Area
        statusArea = new TextArea();
        statusArea.setEditable(false);
        statusArea.setPrefHeight(150);
        statusArea.setWrapText(true);
        statusArea.setText("Listo para generar reportes.");
        VBox.setVgrow(statusArea, Priority.ALWAYS); // Optional

        this.getChildren().addAll(titulo, periodBox, btnBox, statusArea);
    }

    private void generateReport() {
        PeriodoReporte periodo = comboPeriodo.getValue();
        if (periodo == null) return;

        statusArea.setText("Generando reporte " + periodo.getDescripcion() + "...");

        new Thread(() -> {
            try {
                File file = service.generateReport(periodo);
                javafx.application.Platform.runLater(() -> {
                    statusArea.setText("Reporte generado exitosamente:\n" + file.getAbsolutePath());
                    Alert info = new Alert(Alert.AlertType.INFORMATION);
                    info.setTitle("Reporte Generado");
                    info.setHeaderText(null);
                    info.setContentText("El reporte se ha guardado correctamente.");
                    info.showAndWait();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    statusArea.setText("Error al generar reporte:\n" + ex.getMessage());
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("No se pudo generar el reporte.");
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void openFolder() {
        try {
            File dir = new File("reports/sales");
            if (!dir.exists()) dir.mkdirs();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
            } else {
                statusArea.setText("No se puede abrir la carpeta automáticamente en este sistema.");
            }
        } catch (Exception ex) {
            statusArea.setText("Error al abrir carpeta: " + ex.getMessage());
        }
    }
}

