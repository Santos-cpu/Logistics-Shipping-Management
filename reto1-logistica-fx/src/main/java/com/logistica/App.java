package com.logistica;

import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(App.class.getResource("vista.fxml"), "No se encontró vista.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        String css = Objects.requireNonNull(
                App.class.getResource("styles.css"), "No se encontró styles.css"
        ).toExternalForm();
        scene.getStylesheets().add(css);

        stage.setTitle("Gestión de Envíos Logísticos");
        stage.setMinWidth(520);
        stage.setMinHeight(540);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}