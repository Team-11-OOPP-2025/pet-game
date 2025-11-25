package com.eleven.pet;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;

import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private PetController controller;
    private GameClock clock;
    private WeatherSystem weatherSystem;
    private AnimationTimer gameLoop;
    private Timeline weatherTimer;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create GameClock and WeatherSystem
        clock = new GameClock();
        weatherSystem = new WeatherSystem();
        
        // Create Model
        PetModel model = new PetModel("MyPet", weatherSystem, clock);
        
        // Create Controller with dependencies
        controller = new PetController(model, clock, weatherSystem, null);
        
        // Create View
        PetView view = new PetView(model, controller, clock, weatherSystem);
        javafx.scene.layout.Pane root = view.initializeUI();
        
        // Setup Scene and Stage
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Pet Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
        
        // Start game loop
        controller.startGameLoop();
    }
    
    @Override
    public void stop() throws Exception {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (weatherTimer != null) {
            weatherTimer.stop();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

