module org.cecade.demoinv {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;
    requires com.github.librepdf.openpdf;

    opens org.cecade.demoinv to javafx.fxml, com.google.gson;
    exports org.cecade.demoinv;
    exports org.cecade.demoinv.products;
    opens org.cecade.demoinv.products to com.google.gson, javafx.fxml;
    opens org.cecade.demoinv.sales to com.google.gson;
    opens org.cecade.demoinv.inventory to com.google.gson;
    exports org.cecade.demoinv.images;
    opens org.cecade.demoinv.images to com.google.gson, javafx.fxml;
}