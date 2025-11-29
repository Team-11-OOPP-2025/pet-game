package com.eleven.pet;

import com.eleven.pet.config.GameItems;
import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create environment systems
        GameClock clock = new GameClock();
        WeatherSystem weatherSystem = new WeatherSystem();

        // Initialize all Items
        GameItems.init();

        // Create Model using Factory
        PetModel model = PetFactory.createNewPet("Björni", weatherSystem, clock);

        // Create Controller
        PetController controller = new PetController(model, clock, weatherSystem, null);

        // Create View
        PetView view = new PetView(model, controller, clock, weatherSystem);
        Pane root = view.initializeUI();

        // Setup Scene and Stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Pet Game - Björni");
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

