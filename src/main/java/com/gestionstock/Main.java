package com.gestionstock;

import com.gestionstock.util.DatabaseConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage stage) throws IOException {
        //DatabaseConfig.testerConnection();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/gestionstock/LoginView.fxml")
        );

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(
                getClass().getResource("/com/gestionstock/style.css").toExternalForm()
        );
        stage.setTitle("Gestion Stock IAGE");
        stage.setScene(scene);
        stage.show();
    }
}