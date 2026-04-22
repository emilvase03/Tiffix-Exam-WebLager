module dk.easv.tiffixexamweblager {
    requires javafx.controls;
    requires javafx.fxml;


    opens dk.easv.tiffixexamweblager to javafx.fxml;
    exports dk.easv.tiffixexamweblager;
}