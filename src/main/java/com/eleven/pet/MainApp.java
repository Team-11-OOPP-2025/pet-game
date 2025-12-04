package com.eleven.pet;

import com.eleven.pet.config.GameItems;
import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.view.PetView;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainApp extends Application {

    private final GameClock clock = new GameClock();
    private final WeatherSystem weatherSystem = new WeatherSystem();
    private PetController controller;
    private AnimationTimer gameLoop;
    private Timeline weatherTimer;

    @Override
    public void start(Stage primaryStage) {
        GameItems.init();

        // Create Model using Factory
        PetModel model = PetFactory.createNewPet("Björni", weatherSystem, clock);

        // Create Controller
        controller = new PetController(model, clock, weatherSystem, null);

        // Create View
        PetView view = new PetView(model, controller, clock, weatherSystem);
        Pane root = view.initializeUI();

        final long[] lastUpdate = {System.nanoTime()};
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaSeconds = (now - lastUpdate[0]) / 1_000_000_000.0;
                lastUpdate[0] = now;

                boolean newDayStarted = clock.tick(deltaSeconds);

                if (newDayStarted) {
                    model.replenishDailyFood();
                }
            }
        };
        gameLoop.start();

        // Weather changes every 30 seconds
        weatherTimer = new Timeline(new KeyFrame(
                Duration.seconds(30),
                event -> {
                    weatherSystem.changeWeather();
                    System.out.println("Weather changed to: " + weatherSystem.getCurrentWeather().getName());
                }
        ));
        weatherTimer.setCycleCount(Timeline.INDEFINITE);
        weatherTimer.play();

        controller.initAutosave();

        // Setup Scene and Stage
        Scene scene = new Scene(root, 1920, 1080);
        primaryStage.setTitle("Pet Game - Björni");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        // maintains a proper aspect ratio of 1:0.75 for 980x720 window
        primaryStage.setHeight(1080);
        primaryStage.setWidth(1920);
        primaryStage.show();


    }

    @Override
    public void stop() {
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

    public static void initializeApplication(String[] args) {
        launch(args);
    }
}

