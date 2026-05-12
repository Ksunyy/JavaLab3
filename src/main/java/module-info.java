module com.lab2.javalab3 {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.lab2.javalab3 to javafx.fxml;
    exports com.lab2.javalab3;
}