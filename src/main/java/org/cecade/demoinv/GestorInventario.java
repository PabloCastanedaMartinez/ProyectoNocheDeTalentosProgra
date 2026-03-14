package org.cecade.demoinv;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class GestorInventario extends Application {

    private ProductoRepository repo;
    private TabPane tabPane;
    private HBox cardsBox;
    private ComboBox<String> criterioCB;
    private Producto productoSeleccionado;

    @Override
    public void start(Stage stage) {
        repo = new ProductoRepository();

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // Dashboard tab (not closable)
        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(crearDashboard());

        tabPane.getTabs().add(dashboardTab);

        StackPane root = new StackPane(tabPane);
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(
                GestorInventario.class.getResource("styles.css").toExternalForm()
        );

        stage.setTitle("Sistema de Gestión de Inventario");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        refrescarCards();
    }

    private VBox crearDashboard() {
        VBox mainPanel = new VBox(20);
        mainPanel.getStyleClass().add("main-panel");
        mainPanel.setAlignment(Pos.CENTER);
        mainPanel.setPadding(new Insets(25));

        // Criterio selector
        HBox criterioBox = new HBox(10);
        criterioBox.setAlignment(Pos.CENTER_LEFT);
        Label criterioLabel = new Label("Mostrar:");
        criterioLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        criterioCB = new ComboBox<>();
        criterioCB.getItems().addAll("Top Vendidos (últimos 30 días)", "Mayor Stock");
        criterioCB.setValue("Top Vendidos (últimos 30 días)");
        criterioCB.getStyleClass().add("combo-criterio");
        criterioCB.setOnAction(e -> refrescarCards());
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
        btnBuscar.setOnAction(e -> abrirBusqueda());

        Button btnVender = new Button("Vender");
        btnVender.getStyleClass().add("btn-vender");
        btnVender.setOnAction(e -> realizarVenta());

        Button btnIngresar = new Button("Ingresar Producto");
        btnIngresar.getStyleClass().add("btn-ingresar");
        btnIngresar.setOnAction(e -> abrirIngresarProducto());

        buttonsBox.getChildren().addAll(btnBuscar, btnVender, btnIngresar);

        mainPanel.getChildren().addAll(criterioBox, cardsBox, spacer, buttonsBox);
        return mainPanel;
    }

    private void refrescarCards() {
        cardsBox.getChildren().clear();

        String criterio = criterioCB.getValue();
        List<Producto> topProducts;
        if ("Mayor Stock".equals(criterio)) {
            topProducts = repo.mayorStock(3);
        } else {
            topProducts = repo.topVendidos(3);
        }

        for (Producto p : topProducts) {
            VBox card = crearCard(p);
            cardsBox.getChildren().add(card);
        }
    }

    private VBox crearCard(Producto p) {
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

        // Click to select
        card.setOnMouseClicked(e -> {
            productoSeleccionado = p;
            card.setStyle("-fx-border-color: #5f9ea0; -fx-border-width: 3; -fx-border-radius: 10;");
            // Reset others
            for (var node : cardsBox.getChildren()) {
                if (node != card && node instanceof VBox v) {
                    v.setStyle("");
                }
            }
        });

        return card;
    }

    // ========== BUSCAR PRODUCTO ==========

    private void abrirBusqueda() {
        // Check if search tab already open
        for (Tab tab : tabPane.getTabs()) {
            if ("Búsqueda".equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Tab searchTab = new Tab("Búsqueda");
        searchTab.setContent(crearPanelBusqueda());
        tabPane.getTabs().add(searchTab);
        tabPane.getSelectionModel().select(searchTab);
    }

    private VBox crearPanelBusqueda() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("main-panel");
        panel.setPadding(new Insets(20));

        // Search bar
        HBox searchBar = new HBox(10);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Buscar por nombre...");
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button btnSearch = new Button("Buscar");
        btnSearch.getStyleClass().add("btn-buscar");
        btnSearch.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");

        searchBar.getChildren().addAll(searchField, btnSearch);

        // Results table
        TableView<Producto> tabla = new TableView<>();
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tabla, Priority.ALWAYS);

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

        tabla.getColumns().addAll(colNombre, colStock, colVentas, colPrecio);

        // Product detail pane
        VBox fichaBox = new VBox(10);
        fichaBox.getStyleClass().add("ficha-producto");
        fichaBox.setVisible(false);
        fichaBox.setManaged(false);

        Label fichaTitulo = new Label();
        fichaTitulo.getStyleClass().add("ficha-titulo");

        ImageView fichaImg = new ImageView();
        fichaImg.setFitWidth(180);
        fichaImg.setFitHeight(140);
        fichaImg.setPreserveRatio(true);

        Label fichaStock = new Label();
        fichaStock.getStyleClass().add("ficha-label");
        Label fichaVentas = new Label();
        fichaVentas.getStyleClass().add("ficha-label");
        Label fichaPrecio = new Label();
        fichaPrecio.getStyleClass().add("ficha-label");

        Button btnVenderSearch = new Button("Vender este producto");
        btnVenderSearch.getStyleClass().add("btn-vender");
        btnVenderSearch.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");

        Button btnAgregarStockSearch = new Button("Agregar Stock");
        btnAgregarStockSearch.getStyleClass().add("btn-ingresar");
        btnAgregarStockSearch.setStyle("-fx-font-size: 13px; -fx-padding: 8 20;");

        fichaBox.getChildren().addAll(fichaTitulo, fichaImg, fichaStock, fichaVentas, fichaPrecio, btnVenderSearch, btnAgregarStockSearch);

        // Table selection listener
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                productoSeleccionado = newVal;
                fichaTitulo.setText(newVal.getNombre());
                fichaImg.setImage(ImageHelper.cargarImagen(newVal.getRutaImagenLocal(), 180, 140));
                fichaStock.setText("Existencias: " + newVal.getStock());
                fichaVentas.setText("Ventas últimos 30 días: " + newVal.getVentasUltimos30Dias());
                fichaPrecio.setText("Precio unitario: Q " + newVal.getPrecioUnitario().toPlainString());
                fichaBox.setVisible(true);
                fichaBox.setManaged(true);
            }
        });

        btnVenderSearch.setOnAction(e -> {
            if (productoSeleccionado != null) {
                realizarVentaProducto(productoSeleccionado);
                // Refresh table
                doSearch(searchField.getText(), tabla);
                // Re-select to refresh ficha
                tabla.getSelectionModel().clearSelection();
                refrescarCards();
            }
        });

        btnAgregarStockSearch.setOnAction(e -> {
            if (productoSeleccionado != null) {
                agregarStockProducto(productoSeleccionado);
                // Refresh table
                doSearch(searchField.getText(), tabla);
                tabla.getSelectionModel().clearSelection();
                refrescarCards();
            }
        });

        // Search action
        Runnable doSearchAction = () -> doSearch(searchField.getText(), tabla);
        btnSearch.setOnAction(e -> doSearchAction.run());
        searchField.setOnAction(e -> doSearchAction.run());

        // Load all initially
        tabla.getItems().addAll(repo.getAll());

        // Layout: table left, ficha right
        HBox content = new HBox(15);
        content.setAlignment(Pos.TOP_LEFT);
        VBox.setVgrow(content, Priority.ALWAYS);
        HBox.setHgrow(tabla, Priority.ALWAYS);
        content.getChildren().addAll(tabla, fichaBox);

        panel.getChildren().addAll(searchBar, content);
        return panel;
    }

    private void doSearch(String text, TableView<Producto> tabla) {
        tabla.getItems().clear();
        if (text == null || text.isBlank()) {
            tabla.getItems().addAll(repo.getAll());
        } else {
            tabla.getItems().addAll(repo.buscar(text));
        }
    }

    // ========== VENDER ==========

    private void realizarVenta() {
        if (productoSeleccionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seleccionar Producto");
            alert.setHeaderText(null);
            alert.setContentText("Primero selecciona un producto haciendo clic en una tarjeta del dashboard o buscándolo en la pestaña de Búsqueda.");
            alert.showAndWait();
            return;
        }
        realizarVentaProducto(productoSeleccionado);
        refrescarCards();
    }

    private void realizarVentaProducto(Producto producto) {
        // Refresh product from repo
        Producto p = repo.getAll().stream()
                .filter(pr -> pr.getId() == producto.getId())
                .findFirst().orElse(producto);

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Vender - " + p.getNombre());
        dialog.setHeaderText("Producto: " + p.getNombre() + "\nStock disponible: " + p.getStock());
        dialog.setContentText("¿Cuántas unidades vendiste?");

        dialog.showAndWait().ifPresent(input -> {
            int cantidad;
            try {
                cantidad = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                mostrarError("Entrada inválida", "Debes ingresar un número entero válido.");
                return;
            }

            if (cantidad <= 0) {
                mostrarError("Cantidad inválida", "La cantidad debe ser mayor que 0.");
                return;
            }

            if (cantidad > p.getStock()) {
                mostrarError("Stock insuficiente",
                        "No tienes suficientes existencias para esa venta.\nStock disponible: " + p.getStock());
                return;
            }

            // Process sale
            p.setStock(p.getStock() - cantidad);
            p.setVentasUltimos30Dias(p.getVentasUltimos30Dias() + cantidad);
            repo.actualizar(p);

            var total = p.getPrecioUnitario().multiply(java.math.BigDecimal.valueOf(cantidad));

            Alert confirm = new Alert(Alert.AlertType.INFORMATION);
            confirm.setTitle("Venta Exitosa");
            confirm.setHeaderText("¡Venta registrada!");
            confirm.setContentText(
                    "Producto: " + p.getNombre() +
                            "\nCantidad vendida: " + cantidad +
                            "\nEsta venta debería generar: Q " + total.toPlainString() +
                            "\nStock restante: " + p.getStock()
            );
            confirm.showAndWait();

            productoSeleccionado = p;
            refrescarCards();
        });
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    // ========== AGREGAR STOCK ==========

    private void agregarStockProducto(Producto producto) {
        // Refresh product from repo
        Producto p = repo.getAll().stream()
                .filter(pr -> pr.getId() == producto.getId())
                .findFirst().orElse(producto);

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Agregar Stock - " + p.getNombre());
        dialog.setHeaderText("Producto: " + p.getNombre() + "\nStock actual: " + p.getStock());
        dialog.setContentText("¿Cuántas unidades deseas agregar?");

        dialog.showAndWait().ifPresent(input -> {
            int cantidad;
            try {
                cantidad = Integer.parseInt(input.trim());
            } catch (NumberFormatException ex) {
                mostrarError("Entrada inválida", "Debes ingresar un número entero válido.");
                return;
            }

            if (cantidad <= 0) {
                mostrarError("Cantidad inválida", "La cantidad debe ser mayor que 0.");
                return;
            }

            int stockAnterior = p.getStock();
            boolean resultado = repo.agregarStock(p.getId(), cantidad);

            if (resultado) {
                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("Stock Actualizado");
                confirm.setHeaderText("¡Stock agregado exitosamente!");
                confirm.setContentText(
                        "Producto: " + p.getNombre() +
                                "\nCantidad agregada: " + cantidad +
                                "\nStock anterior: " + stockAnterior +
                                "\nStock nuevo: " + (stockAnterior + cantidad)
                );
                confirm.showAndWait();
            } else {
                mostrarError("Error", "No se pudo agregar stock al producto.");
            }

            productoSeleccionado = p;
            refrescarCards();
        });
    }

    // ========== INGRESAR PRODUCTO ==========

    private void abrirIngresarProducto() {
        // Check if tab already open
        for (Tab tab : tabPane.getTabs()) {
            if ("Ingresar Producto".equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Tab ingresoTab = new Tab("Ingresar Producto");
        ingresoTab.setContent(crearPanelIngreso(ingresoTab));
        tabPane.getTabs().add(ingresoTab);
        tabPane.getSelectionModel().select(ingresoTab);
    }

    private VBox crearPanelIngreso(Tab tab) {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("main-panel");
        panel.setPadding(new Insets(25));
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMaxWidth(500);

        Label titulo = new Label("Nuevo Producto");
        titulo.getStyleClass().add("ficha-titulo");

        // Form fields
        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setAlignment(Pos.CENTER);

        Label lblNombre = new Label("Nombre:");
        lblNombre.getStyleClass().add("form-label");
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del producto");

        Label lblPrecio = new Label("Precio Unitario:");
        lblPrecio.getStyleClass().add("form-label");
        TextField txtPrecio = new TextField();
        txtPrecio.setPromptText("Ej: 199.99");

        Label lblStock = new Label("Stock Inicial:");
        lblStock.getStyleClass().add("form-label");
        TextField txtStock = new TextField();
        txtStock.setPromptText("Ej: 50");

        Label lblVentas = new Label("Ventas 30 días:");
        lblVentas.getStyleClass().add("form-label");
        TextField txtVentas = new TextField("0");
        txtVentas.setPromptText("Default: 0");

        Label lblImagen = new Label("Imagen:");
        lblImagen.getStyleClass().add("form-label");

        HBox imgBox = new HBox(10);
        imgBox.setAlignment(Pos.CENTER_LEFT);
        TextField txtImagen = new TextField();
        txtImagen.setPromptText("Ruta de imagen o seleccionar...");
        txtImagen.setPrefWidth(250);

        Button btnFileChooser = new Button("Examinar...");
        btnFileChooser.setOnAction(e -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Seleccionar Imagen del Producto");
            fc.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
            );
            java.io.File file = fc.showOpenDialog(tabPane.getScene().getWindow());
            if (file != null) {
                txtImagen.setText(file.getAbsolutePath());
            }
        });
        imgBox.getChildren().addAll(txtImagen, btnFileChooser);

        // Preview
        ImageView preview = new ImageView();
        preview.setFitWidth(120);
        preview.setFitHeight(100);
        preview.setPreserveRatio(true);

        txtImagen.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isBlank()) {
                preview.setImage(ImageHelper.cargarImagen(n, 120, 100));
            }
        });

        form.add(lblNombre, 0, 0);
        form.add(txtNombre, 1, 0);
        form.add(lblPrecio, 0, 1);
        form.add(txtPrecio, 1, 1);
        form.add(lblStock, 0, 2);
        form.add(txtStock, 1, 2);
        form.add(lblVentas, 0, 3);
        form.add(txtVentas, 1, 3);
        form.add(lblImagen, 0, 4);
        form.add(imgBox, 1, 4);
        form.add(new Label("Vista previa:"), 0, 5);
        form.add(preview, 1, 5);

        // Save button
        Button btnGuardar = new Button("Guardar Producto");
        btnGuardar.getStyleClass().add("btn-ingresar");
        btnGuardar.setOnAction(e -> {
            String nombre = txtNombre.getText();
            String precioStr = txtPrecio.getText();
            String stockStr = txtStock.getText();
            String ventasStr = txtVentas.getText();
            String imagen = txtImagen.getText();

            // Validate
            if (nombre == null || nombre.isBlank()) {
                mostrarError("Validación", "El nombre no puede estar vacío.");
                return;
            }

            if (repo.existeProducto(nombre)) {
                mostrarError("Producto Duplicado",
                        "Ya existe un producto con el nombre \"" + nombre.trim() + "\" en el inventario.\n" +
                        "Si deseas actualizar su stock, usa la opción de búsqueda.");
                return;
            }

            java.math.BigDecimal precio;
            try {
                precio = new java.math.BigDecimal(precioStr.trim());
                if (precio.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                    mostrarError("Validación", "El precio debe ser mayor que 0.");
                    return;
                }
            } catch (Exception ex) {
                mostrarError("Validación", "El precio debe ser un número válido.");
                return;
            }

            int stock;
            try {
                stock = Integer.parseInt(stockStr.trim());
                if (stock < 0) {
                    mostrarError("Validación", "El stock no puede ser negativo.");
                    return;
                }
            } catch (NumberFormatException ex) {
                mostrarError("Validación", "El stock debe ser un número entero válido.");
                return;
            }

            int ventas;
            try {
                ventas = Integer.parseInt(ventasStr.trim());
                if (ventas < 0) ventas = 0;
            } catch (NumberFormatException ex) {
                ventas = 0;
            }

            if (imagen == null || imagen.isBlank()) {
                imagen = "images/placeholder.png";
            }

            Producto nuevo = new Producto(nombre.trim(), precio, stock, ventas, imagen);
            repo.agregar(nuevo);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Producto Guardado");
            ok.setHeaderText(null);
            ok.setContentText("El producto \"" + nombre.trim() + "\" fue guardado exitosamente.");
            ok.showAndWait();

            // Clear form
            txtNombre.clear();
            txtPrecio.clear();
            txtStock.clear();
            txtVentas.setText("0");
            txtImagen.clear();

            refrescarCards();
        });

        panel.getChildren().addAll(titulo, form, btnGuardar);

        // Wrap in scroll + center
        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        VBox wrapper = new VBox(scroll);
        wrapper.setAlignment(Pos.TOP_CENTER);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        return wrapper;
    }
}
