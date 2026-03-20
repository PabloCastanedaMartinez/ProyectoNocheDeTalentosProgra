package org.cecade.demoinv;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.cecade.demoinv.products.Producto;
import org.cecade.demoinv.services.InventoryService;
import org.cecade.demoinv.ui.*;

import java.util.Objects;
import java.util.function.Supplier;

public class GestorInventario extends Application {

    private InventoryService service;
    private TabPane tabPane;
    private DashboardView dashboardView;
    private Producto dashboardSelectedProduct;

    @Override
    public void start(Stage stage) {
        service = new InventoryService();

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);

        // Dashboard tab
        createDashboard();

        StackPane root = new StackPane(tabPane);
        root.getStyleClass().add("root-pane");
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 900, 650);
        scene.getStylesheets().add(
                Objects.requireNonNull(GestorInventario.class.getResource("styles.css")).toExternalForm()
        );

        stage.setTitle("Sistema de Gestión de Inventario");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    private void createDashboard() {
        dashboardView = new DashboardView(
                service,
                this::onDashboardProductSelected,
                this::openSearch,
                this::sellFromDashboard,
                this::openProductEntry,
                this::openReports
        );

        Tab dashboardTab = new Tab("Dashboard");
        dashboardTab.setClosable(false);
        dashboardTab.setContent(dashboardView);
        tabPane.getTabs().add(dashboardTab);
    }

    private void onDashboardProductSelected(Producto p) {
        this.dashboardSelectedProduct = p;
    }

    private void sellFromDashboard() {
        if (dashboardSelectedProduct == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Seleccionar Producto");
            alert.setHeaderText(null);
            alert.setContentText("Primero selecciona un producto haciendo clic en una tarjeta del dashboard.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Vender - " + dashboardSelectedProduct.getNombre());
        dialog.setHeaderText("Producto: " + dashboardSelectedProduct.getNombre() + "\nStock disponible: " + dashboardSelectedProduct.getStock());
        dialog.setContentText("¿Cuántas unidades vendiste?");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int cantidad = Integer.parseInt(input.trim());
                service.sellProduct(dashboardSelectedProduct.getId(), cantidad);

                // Refresh data
                Producto fresh = service.getProductById(dashboardSelectedProduct.getId()).orElse(dashboardSelectedProduct);
                dashboardSelectedProduct = fresh; // Update reference

                Alert confirm = new Alert(Alert.AlertType.INFORMATION);
                confirm.setTitle("Venta Exitosa");
                confirm.setHeaderText("¡Venta registrada!");

                confirm.setContentText(
                        "Producto: " + fresh.getNombre() +
                                "\nCantidad vendida: " + cantidad +
                                "\nStock restante: " + fresh.getStock()
                );
                confirm.showAndWait();

                refreshAll();

            } catch (NumberFormatException ex) {
                showError("Entrada inválida", "Debes ingresar un número entero válido.");
            } catch (Exception ex) {
                showError("Error al vender", ex.getMessage());
            }
        });
    }

    private void openSearch() {
        selectOrAddTab("Búsqueda", () -> new SearchView(service, this::refreshAll));
    }

    private void openProductEntry() {
        selectOrAddTab("Ingresar Producto", () -> new ProductFormView(service, this::refreshAll));
    }

    private void openReports() {
        selectOrAddTab("Reportes", () -> new ReportView(service));
    }

    private void selectOrAddTab(String title, Supplier<Node> contentSupplier) {
        for (Tab tab : tabPane.getTabs()) {
            if (title.equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                return;
            }
        }

        Tab newTab = new Tab(title);
        // We create content only if tab needs to be created
        newTab.setContent(contentSupplier.get());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    private void refreshAll() {
        if (dashboardView != null) {
            dashboardView.refreshCards();
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
