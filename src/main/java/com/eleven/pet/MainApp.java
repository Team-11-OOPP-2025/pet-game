package com.eleven.pet;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // Create Model
        PetModel model = new PetModel("MyPet");
        
        // Create Controller
        PetController controller = new PetController(model, null);
        
        // Create View
        PetView view = new PetView(model, controller);
        Pane root = view.initializeUI();
        
        // Setup Scene and Stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Pet Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        
        // Start game loop
        controller.startGameLoop();
    }

    public static void initializeApplication(String[] args) {
        launch(args);
    }
}

