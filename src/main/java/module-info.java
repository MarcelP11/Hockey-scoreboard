module com.example.scoreboard {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.scoreboard to javafx.fxml;
    exports com.example.scoreboard;
}