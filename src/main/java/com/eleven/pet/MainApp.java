package com.eleven.pet;

import com.eleven.pet.view.PetView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            PetView view = new PetView(null, null);
            Pane root = view.initializeUI();

            Scene scene = new Scene(root, 400, 600);

            primaryStage.setTitle("Pet");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal Error: Could not start application. Check KeyLoader or JavaFX setup.");
        }
    }

    /// Initializes and launches the JavaFX application.
    public static void initializeApplication(String[] args) {
        launch(args);
    }
}

