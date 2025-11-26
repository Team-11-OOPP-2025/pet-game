package com.eleven.pet;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {
    private PetController controller;
    private GameClock clock;
    private WeatherSystem weatherSystem;
    private PetModel model;
    private AnimationTimer gameLoop;
    private Timeline weatherTimer;
    private long lastUpdateTime = 0;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create GameClock and WeatherSystem
        clock = new GameClock();
        weatherSystem = new WeatherSystem();
        
        // Create Model
        model = new PetModel("MyPet", weatherSystem, clock);
        
        // Subscribe model to time and weather updates
        clock.subscribe(model);
        weatherSystem.subscribe(model);
        
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
        startGameLoop();
        
        // Start weather change timer
        startWeatherTimer();
        
        // Initialize autosave
        controller.initAutosave();
    }
    
    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }
                
                // Calculate elapsed time in seconds
                double elapsedSeconds = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                // Update the game clock
                if (clock != null) {
                    boolean newDayStarted = clock.tick(elapsedSeconds);
                    
                    if (newDayStarted) {
                        model.replenishDailyFood();
                    }
                }
            }
        };
        
        gameLoop.start();
        System.out.println("✓ Game loop started!");
    }
    
    private void startWeatherTimer() {
        weatherTimer = new Timeline(new KeyFrame(
            Duration.seconds(GameConfig.WEATHER_CHANGE_INTERVAL),
            event -> weatherSystem.changeWeather()
        ));
        weatherTimer.setCycleCount(Timeline.INDEFINITE);
        weatherTimer.play();
        System.out.println("✓ Weather timer started!");
    }
    
    @Override
    public void stop() throws Exception {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        if (weatherTimer != null) {
            weatherTimer.stop();
        }
        if (controller != null) {
            controller.shutdown();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

