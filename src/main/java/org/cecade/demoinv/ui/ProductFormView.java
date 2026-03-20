package org.cecade.demoinv.ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.cecade.demoinv.images.ImageHelper;
import org.cecade.demoinv.services.InventoryService;

import java.io.File;
import java.math.BigDecimal;

public class ProductFormView extends VBox {

    private final InventoryService service;
    private final Runnable onProductSaved;

    // Fields
    private TextField txtNombre;
    private TextField txtPrecio;
    private TextField txtStock;
    private TextField txtVentas;
    private TextField txtImagen;
    private ImageView preview;

    public ProductFormView(InventoryService service, Runnable onProductSaved) {
        this.service = service;
        this.onProductSaved = onProductSaved;
        initUI();
    }

    private void initUI() {
        this.setSpacing(15);
        this.getStyleClass().add("main-panel");
        this.setPadding(new javafx.geometry.Insets(25));
        this.setAlignment(Pos.TOP_CENTER);
        this.setMaxWidth(500);

        Label titulo = new Label("Nuevo Producto");
        titulo.getStyleClass().add("ficha-titulo");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setAlignment(Pos.CENTER);

        // Labels and Inputs
        Label lblNombre = new Label("Nombre:");
        lblNombre.getStyleClass().add("form-label");
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del producto");

        Label lblPrecio = new Label("Precio Unitario:");
        lblPrecio.getStyleClass().add("form-label");
        txtPrecio = new TextField();
        txtPrecio.setPromptText("Ej: 199.99");

        Label lblCosto = new Label("Costo Inicial:");
        lblCosto.getStyleClass().add("form-label");
        TextField txtCosto = new TextField();
        txtCosto.setPromptText("Ej: 100.00");

        Label lblStock = new Label("Stock Inicial:");
        lblStock.getStyleClass().add("form-label");
        txtStock = new TextField();
        txtStock.setPromptText("Ej: 50");

        Label lblVentas = new Label("Ventas 30 días:");
        lblVentas.getStyleClass().add("form-label");
        txtVentas = new TextField("0");
        txtVentas.setPromptText("Default: 0");

        Label lblImagen = new Label("Imagen:");
        lblImagen.getStyleClass().add("form-label");

        HBox imgBox = new HBox(10);
        imgBox.setAlignment(Pos.CENTER_LEFT);
        txtImagen = new TextField();
        txtImagen.setPromptText("Ruta de imagen o seleccionar...");
        txtImagen.setPrefWidth(250);

        Button btnFileChooser = new Button("Examinar...");
        btnFileChooser.setOnAction(e -> chooseImage());
        imgBox.getChildren().addAll(txtImagen, btnFileChooser);

        preview = new ImageView();
        preview.setFitWidth(120);
        preview.setFitHeight(100);
        preview.setPreserveRatio(true);

        txtImagen.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.isBlank()) {
                preview.setImage(ImageHelper.cargarImagen(n, 120, 100));
            }
        });

        // Add to grid
        form.add(lblNombre, 0, 0);
        form.add(txtNombre, 1, 0);
        form.add(lblPrecio, 0, 1);
        form.add(txtPrecio, 1, 1);
        form.add(lblCosto, 0, 2);
        form.add(txtCosto, 1, 2);
        form.add(lblStock, 0, 3);
        form.add(txtStock, 1, 3);
        form.add(lblVentas, 0, 4);
        form.add(txtVentas, 1, 4);
        form.add(lblImagen, 0, 5);
        form.add(imgBox, 1, 5);
        form.add(new Label("Vista previa:"), 0, 6);
        form.add(preview, 1, 6);

        Button btnGuardar = new Button("Guardar Producto");
        btnGuardar.getStyleClass().add("btn-ingresar");
        btnGuardar.setOnAction(e -> saveProduct(txtCosto));

        this.getChildren().addAll(titulo, form, btnGuardar);
    }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar Imagen del Producto");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        File file = fc.showOpenDialog(this.getScene().getWindow());
        if (file != null) {
            txtImagen.setText(file.getAbsolutePath());
        }
    }

    private void saveProduct(TextField txtCosto) {
        try {
            String nombre = txtNombre.getText();
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            BigDecimal costoOriginal = txtCosto.getText().isBlank() ? BigDecimal.ZERO : new BigDecimal(txtCosto.getText().trim());
            int stock = Integer.parseInt(txtStock.getText().trim());
            int ventas = Integer.parseInt(txtVentas.getText().trim());
            String imagen = txtImagen.getText();

            service.createProduct(nombre, precio, stock, ventas, imagen, costoOriginal);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Producto Guardado");
            ok.setHeaderText(null);
            ok.setContentText("El producto \"" + nombre.trim() + "\" fue guardado exitosamente.");
            ok.showAndWait();

            clearForm(txtCosto);
            if (onProductSaved != null) {
                onProductSaved.run();
            }

        } catch (NumberFormatException e) {
            showError("Error de validación", "Por favor verifica los campos numéricos.");
        } catch (Exception e) {
            showError("Error al guardar", e.getMessage());
        }
    }

    private void clearForm(TextField txtCosto) {
        txtNombre.clear();
        txtPrecio.clear();
        txtStock.clear();
        txtVentas.setText("0");
        txtCosto.clear();
        txtImagen.clear();
        preview.setImage(null);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

