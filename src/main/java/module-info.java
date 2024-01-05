module com.example.groupproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires com.dlsc.formsfx;

    opens com.example.groupproject to javafx.fxml;
    exports com.example.groupproject;
}