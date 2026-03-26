package org.cecade.demoinv.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.cecade.demoinv.images.ImageHelper;
import org.cecade.demoinv.products.Producto;
import org.cecade.demoinv.services.InventoryService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SearchView extends VBox {

    private final InventoryService service;
    private final Runnable onDataChanged;

    private TextField searchField;
    private TableView<Producto> tabla;
    private Producto productoSeleccionado;

    // Ficha details
    private VBox fichaBox;
    private Label fichaTitulo;
    private ImageView fichaImg;
    private Label fichaStock;
    private Label fichaVentas;
    private Label fichaPrecio;
    private Label fichaPromo;

    public SearchView(InventoryService service, Runnable onDataChanged) {
        this.service = service;
        this.onDataChanged = onDataChanged;
        initUI();
    }

    private void initUI() {
        this.setSpacing(15);
        this.getStyleClass().add("main-panel");
        this.setPadding(new Insets(20));

        Button btnRefreshSearch = new Button("Refrescar");
        btnRefreshSearch.getStyleClass().add("btn-refrescar");
        btnRefreshSearch.setStyle("-fx-font-size: 13px; -fx-padding: 8 20");

        btnRefreshSearch.setOnAction(e -> {
            searchField.clear();

            fichaBox.setVisible(false);
            fichaBox.setManaged(false);
            productoSeleccionado = null;

            performSearch();
        });

        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button btnSearch = new Button("Buscar");
        btnSearch.getStyleClass().add("btn-buscar");
        btnSearch.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");

        // Action
        Runnable doSearchAction = this::performSearch;
        btnSearch.setOnAction(e -> doSearchAction.run());
        searchField.setOnAction(e -> doSearchAction.run());

        searchBar.getChildren().addAll(searchField, btnSearch, btnRefreshSearch);

        // Results table
        tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tabla, Priority.ALWAYS);
        setupTable();

        // Product detail pane
        setupFicha();

        // Layout
        HBox content = new HBox(15);
        content.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(content, Priority.ALWAYS);
        HBox.setHgrow(tabla, Priority.ALWAYS);
        content.getChildren().addAll(tabla, fichaBox);

        this.getChildren().addAll(searchBar, content);

        // Initial load
        performSearch();
    }

    private void setupTable() {
        TableColumn<Producto, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getNombre()));
        colNombre.setPrefWidth(200);

        TableColumn<Producto, String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getStock())));

        TableColumn<Producto, String> colVentas = new TableColumn<>("Ventas 30d");
        colVentas.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getVentasUltimos30Dias())));

        TableColumn<Producto, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty("Q " + data.getValue().getPrecioUnitario().toPlainString()));

        TableColumn<Producto, String> colPromo = new TableColumn<>("Promoción");
        colPromo.setCellValueFactory(data -> {
            Producto p = data.getValue();
            if (p.isEnPromocion() && p.getFinPromocion() != null) {
                long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), p.getFinPromocion());
                if (dias < 0) return new javafx.beans.property.SimpleStringProperty("Vencida");
                return new javafx.beans.property.SimpleStringProperty(dias + " días restantes");
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });

        tabla.getColumns().addAll(colNombre, colStock, colVentas, colPrecio, colPromo);

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectProduct(newVal);
            }
        });
    }

    private void setupFicha() {
        fichaBox = new VBox(10);
        fichaBox.getStyleClass().add("ficha-producto");
        fichaBox.setVisible(false);
        fichaBox.setManaged(false);

        fichaTitulo = new Label();
        fichaTitulo.getStyleClass().add("ficha-titulo");

        fichaImg = new ImageView();
        fichaImg.setFitWidth(180);
        fichaImg.setFitHeight(140);
        fichaImg.setPreserveRatio(true);

        fichaStock = new Label();
        fichaStock.getStyleClass().add("ficha-label");
        fichaVentas = new Label();
        fichaVentas.getStyleClass().add("ficha-label");
        fichaPrecio = new Label();
        fichaPrecio.getStyleClass().add("ficha-label");
        fichaPromo = new Label();
        fichaPromo.getStyleClass().add("ficha-promo-label"); // New style class if needed or reuse ficha-label

        Button btnVender = new Button("Vender este producto");
        btnVender.getStyleClass().add("btn-vender");
        btnVender.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");
        btnVender.setOnAction(e -> handleSell());

        Button btnAddStock = new Button("Agregar Stock");
        btnAddStock.getStyleClass().add("btn-ingresar");
        btnAddStock.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");
        btnAddStock.setOnAction(e -> handleAddStock());

        Button btnModifyPrice = new Button("Modificar Precio");
        btnModifyPrice.getStyleClass().add("btn-modificar");
        btnModifyPrice.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");
        btnModifyPrice.setOnAction(e -> handleModifyPrice());

        fichaBox.getChildren().addAll(fichaTitulo, fichaImg, fichaStock, fichaVentas, fichaPrecio, fichaPromo, btnVender, btnAddStock, btnModifyPrice);
    }

    private void performSearch() {
        String text = searchField.getText();
        tabla.getItems().clear();
        tabla.getItems().addAll(service.searchProducts(text));
    }

    private void selectProduct(Producto p) {
        // Refresh product data from service to be sure
        service.getProductById(p.getId()).ifPresent(fresh -> {
            productoSeleccionado = fresh;
            fichaTitulo.setText(fresh.getNombre());
            fichaImg.setImage(ImageHelper.cargarImagen(fresh.getRutaImagenLocal(), 180, 140));
            fichaStock.setText("Existencias: " + fresh.getStock());
            fichaVentas.setText("Ventas últimos 30 días: " + fresh.getVentasUltimos30Dias());
            fichaPrecio.setText("Precio unitario: Q " + fresh.getPrecioUnitario().toPlainString());

            if (fresh.isEnPromocion() && fresh.getFinPromocion() != null) {
                long dias = ChronoUnit.DAYS.between(LocalDateTime.now(), fresh.getFinPromocion());
                if (dias >= 0) {
                    fichaPromo.setText("¡EN PROMOCIÓN! Queda " + dias + " días.");
                    fichaPromo.setVisible(true);
                    fichaPromo.setManaged(true);
                    // fichaPromo already has style class "ficha-promo-label" from setupFicha
                } else {
                    fichaPromo.setVisible(false);
                    fichaPromo.setManaged(false);
                }
            } else {
                fichaPromo.setVisible(false);
                fichaPromo.setManaged(false);
            }

            fichaBox.setVisible(true);
            fichaBox.setManaged(true);
        });
    }

    private void handleSell() {
        if (productoSeleccionado == null) return;

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Vender - " + productoSeleccionado.getNombre());
        dialog.setHeaderText("Producto: " + productoSeleccionado.getNombre() + "\nStock disponible: " + productoSeleccionado.getStock());
        dialog.setContentText("¿Cuántas unidades vendiste?");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int cantidad = Integer.parseInt(input.trim());
                service.sellProduct(productoSeleccionado.getId(), cantidad);

                // Show confirmation
                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("Venta Exitosa");
                confirm.setHeaderText("¡Venta registrada!");
                // Re-fetch to get updated values
                Producto updated = service.getProductById(productoSeleccionado.getId()).orElse(productoSeleccionado);
                confirm.setContentText(
                        "Producto: " + updated.getNombre() +
                                "\nCantidad vendida: " + cantidad +
                                "\nStock restante: " + updated.getStock()
                );
                confirm.showAndWait();

                // Refresh UI
                performSearch();
                selectProduct(updated);
                onDataChanged.run();

            } catch (NumberFormatException ex) {
                showError("Entrada inválida", "Debes ingresar un número entero válido.");
            } catch (Exception ex) {
                showError("Error al vender", ex.getMessage());
            }
        });
    }

    private void handleAddStock() {
        if (productoSeleccionado == null) return;

        Dialog<javafx.util.Pair<Integer, BigDecimal>> dialog = new Dialog<>();
        dialog.setTitle("Agregar Stock - " + productoSeleccionado.getNombre());
        dialog.setHeaderText("Producto: " + productoSeleccionado.getNombre() + "\nStock actual: " + productoSeleccionado.getStock());

        ButtonType loginButtonType = new ButtonType("Agregar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField quantityField = new TextField();
        quantityField.setPromptText("Cantidad");
        TextField costField = new TextField();
        costField.setPromptText("Costo Unitario");

        grid.add(new Label("Cantidad:"), 0, 0);
        grid.add(quantityField, 1, 0);
        grid.add(new Label("Costo Unitario:"), 0, 1);
        grid.add(costField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        javafx.application.Platform.runLater(() -> quantityField.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    int qty = Integer.parseInt(quantityField.getText().trim());
                    String costText = costField.getText().trim();
                    BigDecimal cost = costText.isEmpty() ? BigDecimal.ZERO : new BigDecimal(costText);
                    return new javafx.util.Pair<>(qty, cost);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                int cantidad = result.getKey();
                BigDecimal costo = result.getValue();

                service.addStock(productoSeleccionado.getId(), cantidad, costo);

                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("Stock Actualizado");
                confirm.setHeaderText("¡Stock agregado exitosamente!");

                Producto updated = service.getProductById(productoSeleccionado.getId()).orElse(productoSeleccionado);
                confirm.setContentText(
                        "Producto: " + updated.getNombre() +
                                "\nCantidad agregada: " + cantidad +
                                "\nCosto unitario: Q" + costo +
                                "\nStock nuevo: " + updated.getStock()
                );
                confirm.showAndWait();

                performSearch();
                selectProduct(updated);
                onDataChanged.run();

            } catch (Exception ex) {
                showError("Error al agregar stock", ex.getMessage());
            }
        });
    }

    void handleModifyPrice() {
        if (productoSeleccionado == null) return;

        Dialog<javafx.util.Pair<BigDecimal, javafx.util.Pair<Boolean, Integer>>> dialog = new Dialog<>();
        dialog.setTitle("Modificar Precio - " + productoSeleccionado.getNombre());
        dialog.setHeaderText("Producto: " + productoSeleccionado.getNombre() + "\nPrecio actual: Q " + productoSeleccionado.getPrecioUnitario().toPlainString());

        ButtonType saveButtonType = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField priceField = new TextField();
        priceField.setText(productoSeleccionado.getPrecioUnitario().toPlainString());

        CheckBox promoCheck = new CheckBox("Es promoción");

        TextField daysField = new TextField();
        daysField.setPromptText("Días");
        daysField.setDisable(true);

        promoCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            daysField.setDisable(!newVal);
            if (newVal) {
                daysField.requestFocus();
            }
        });

        if (productoSeleccionado.isEnPromocion()) {
            promoCheck.setSelected(true);
            // Optional: calculate remaining days? For now leaving empty as per requirement just to enable input.
        }

        grid.add(new Label("Nuevo Precio:"), 0, 0);
        grid.add(priceField, 1, 0);
        grid.add(promoCheck, 0, 1);
        grid.add(new Label("Duración (días):"), 0, 2);
        grid.add(daysField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        javafx.application.Platform.runLater(priceField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String priceText = priceField.getText().trim();
                    if (priceText.isEmpty()) return null;
                    BigDecimal price = new BigDecimal(priceText);

                    boolean isPromo = promoCheck.isSelected();
                    int days = 0;
                    if (isPromo) {
                        try {
                            days = Integer.parseInt(daysField.getText().trim());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return new javafx.util.Pair<>(price, new javafx.util.Pair<>(isPromo, days));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            try {
                BigDecimal nuevoPrecio = result.getKey();
                boolean isPromo = result.getValue().getKey();
                int days = result.getValue().getValue();

                service.updatePrice(productoSeleccionado.getId(), nuevoPrecio, isPromo, days);

                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("Precio Actualizado");
                confirm.setHeaderText("¡Precio modificado exitosamente!");

                Producto updated = service.getProductById(productoSeleccionado.getId()).orElse(productoSeleccionado);
                String promoText = isPromo ? "\nEn promoción por " + days + " días" : "";
                confirm.setContentText(
                        "Producto: " + updated.getNombre() +
                                "\nNuevo precio unitario: Q " + updated.getPrecioUnitario().toPlainString() +
                                promoText
                );
                confirm.showAndWait();

                performSearch();
                selectProduct(updated);
                onDataChanged.run();

            } catch (Exception ex) {
                showError("Error al modificar precio", ex.getMessage());
            }
        });
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
