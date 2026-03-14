module org.cecade.demoinv {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;

    opens org.cecade.demoinv to javafx.fxml, com.google.gson;
    exports org.cecade.demoinv;
}