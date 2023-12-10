module com.example.messenger {
    requires javafx.controls;
    requires javafx.fxml;

    requires AnimateFX;

    opens com.example.messenger.client to javafx.fxml;
    exports com.example.messenger.client;
    opens com.example.messenger.server to javafx.fxml;
    exports com.example.messenger.server;
}
