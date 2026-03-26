package org.cecade.demoinv.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.cecade.demoinv.images.ImageHelper;
import org.cecade.demoinv.products.Producto;
import org.cecade.demoinv.services.InventoryService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;

public class DashboardView extends VBox {

    private final InventoryService service;
    private final Consumer<Producto> onProductSelected;
    private final Runnable onSearch;
    private final Runnable onSell;
    private final Runnable onEntry;
    private final Runnable onReport;

    private HBox cardsBox;
    private ComboBox<String> criterioCB;

    public DashboardView(InventoryService service,
                         Consumer<Producto> onProductSelected,
                         Runnable onSearch,
                         Runnable onSell,
                         Runnable onEntry,
                         Runnable onReport) {
        this.service = service;
        this.onProductSelected = onProductSelected;
        this.onSearch = onSearch;
        this.onSell = onSell;
        this.onEntry = onEntry;
        this.onReport = onReport;

        initUI();
        refreshCards();
    }

    private void initUI() {
        this.setSpacing(20);
        this.getStyleClass().add("main-panel");
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(25));

        // Criterio selector
        HBox criterioBox = new HBox(10);
        criterioBox.setAlignment(Pos.CENTER_LEFT);
        Label criterioLabel = new Label("Mostrar:");
        criterioLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        criterioCB = new ComboBox<>();
        criterioCB.getItems().addAll("Top Vendidos (últimos 30 días)", "Mayor Stock");
        criterioCB.setValue("Top Vendidos (últimos 30 días)");
        criterioCB.getStyleClass().add("combo-criterio");
        criterioCB.setOnAction(e -> refreshCards());
        criterioBox.getChildren().addAll(criterioLabel, criterioCB);

        // Cards
        cardsBox = new HBox(25);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPadding(new Insets(10, 0, 10, 0));
        VBox.setVgrow(cardsBox, Priority.ALWAYS);

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Buttons
        HBox buttonsBox = new HBox(30);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10, 0, 0, 0));

        Button btnBuscar = new Button("Buscar Producto");
        btnBuscar.getStyleClass().add("btn-buscar");
        btnBuscar.setOnAction(e -> onSearch.run());

        Button btnVender = new Button("Vender");
        btnVender.getStyleClass().add("btn-vender");
        btnVender.setOnAction(e -> onSell.run());

        Button btnIngresar = new Button("Ingresar Producto");
        btnIngresar.getStyleClass().add("btn-ingresar");
        btnIngresar.setOnAction(e -> onEntry.run());

        Button btnReportes = new Button("Reportes");
        btnReportes.getStyleClass().add("btn-buscar");
        btnReportes.setStyle("-fx-background-color: #6a5acd; -fx-text-fill: white;"); // Custom purple
        btnReportes.setOnAction(e -> onReport.run());

        buttonsBox.getChildren().addAll(btnBuscar, btnVender, btnIngresar, btnReportes);

        this.getChildren().addAll(criterioBox, cardsBox, spacer, buttonsBox);
    }

    public void refreshCards() {
        cardsBox.getChildren().clear();

        String criterio = criterioCB.getValue();
        List<Producto> topProducts;
        if ("Mayor Stock".equals(criterio)) {
            topProducts = service.getTopStock(3);
        } else {
            topProducts = service.getTopSelling(3);
        }

        for (Producto p : topProducts) {
            VBox card = createCard(p);
            cardsBox.getChildren().add(card);
        }
    }

    private VBox createCard(Producto p) {
        VBox card = new VBox(8);
        card.getStyleClass().add("product-card");
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setMinWidth(180);

        // Image container
        StackPane imgContainer = new StackPane();
        imgContainer.getStyleClass().add("image-container");
        imgContainer.setPrefSize(150, 120);
        imgContainer.setMaxSize(150, 120);

        // Note: ImageHelper is in parent package, might need proper import or moving.
        // Assuming org.cecade.demoinv.ImageHelper is accessible if public.
        // I used import org.cecade.demoinv.ImageHelper; above.

        ImageView iv = new ImageView(ImageHelper.cargarImagen(p.getRutaImagenLocal(), 140, 110));
        iv.setFitWidth(140);
        iv.setFitHeight(110);
        iv.setPreserveRatio(true);
        imgContainer.getChildren().add(iv);

        // Labels
        Label nombre = new Label(p.getNombre());
        nombre.getStyleClass().add("product-name");
        nombre.setWrapText(true);

        Label stock = new Label("Cantidad: " + p.getStock());
        stock.getStyleClass().add("product-stock");

        String criterio = criterioCB.getValue();
        Label extra;
        if ("Mayor Stock".equals(criterio)) {
            extra = new Label("Ventas: " + p.getVentasUltimos30Dias());
        } else {
            extra = new Label("Ventas 30d: " + p.getVentasUltimos30Dias());
        }
        extra.getStyleClass().add("product-info-small");

        Label precio = new Label("Q " + p.getPrecioUnitario().toPlainString());
        precio.getStyleClass().add("product-info-small");

        card.getChildren().addAll(imgContainer, nombre, stock, extra, precio);

        if (p.isEnPromocion() && p.getFinPromocion() != null) {
            long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), p.getFinPromocion());
            if (dias >= 0) {
                Label promoLabel = new Label("¡PROMO! Quedan " + dias + " días");
                promoLabel.getStyleClass().add("promo-label");
                card.getChildren().add(promoLabel);
            }
        }

        // Click to select
        card.setOnMouseClicked(e -> {
            highlightCard(card);
            onProductSelected.accept(p);
        });

        return card;
    }

    private void highlightCard(VBox selectedCard) {
        selectedCard.setStyle("-fx-border-color: #5f9ea0; -fx-border-width: 3; -fx-border-radius: 10;");
        for (var node : cardsBox.getChildren()) {
            if (node != selectedCard && node instanceof VBox v) {
                v.setStyle("");
            }
        }
    }
}

