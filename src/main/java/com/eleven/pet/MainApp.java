package com.eleven.pet;

import com.eleven.pet.config.GameItems;
import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.service.persistence.GameException;
import com.eleven.pet.service.persistence.GcmEncryptionService;
import com.eleven.pet.service.persistence.KeyLoader;
import com.eleven.pet.service.persistence.PersistenceService;
import com.eleven.pet.view.PetView;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.crypto.SecretKey;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp extends Application {

    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private static final String APP_TITLE = "Pet Game - Björni";
    private static final String SAVE_FILE_NAME = "savegame.dat";
    private static final double WEATHER_CHANGE_INTERVAL = 30.0;

    private final GameClock clock = new GameClock();
    private final WeatherSystem weatherSystem = new WeatherSystem();
    private final Path savePath = Paths.get(SAVE_FILE_NAME);

    private PetController controller;
    private PetModel model;

    private AnimationTimer gameLoop;
    private Timeline weatherTimer;
    private PersistenceService persistenceService;

    // Tracks the last frame time for delta calculation
    private long lastFrameTime = 0;

    @Override
    public void start(Stage primaryStage) {
        GameItems.init();

        initializePersistence();

        initializeModel();

        controller = new PetController(model, clock, weatherSystem, persistenceService);
        PetView view = new PetView(model, controller, clock, weatherSystem);
        Pane root = view.initializeUI();

        startGameLoop();
        startWeatherSystem();
        controller.initAutosave();

        configureStage(primaryStage, root);
    }

    /**
     * Sets up the PersistenceService with encryption keys.
     * Falls back to generating a deterministic dev key if no key is configured.
     */
    private void initializePersistence() {
        try {
            SecretKey key;
            try {
                key = KeyLoader.loadKey();
            } catch (GameException e) {
                System.out.println("No encryption key found. Generating development key.");
                key = KeyLoader.generateDevKey();
            }

            GcmEncryptionService encryptionService = new GcmEncryptionService(key);
            persistenceService = new PersistenceService(encryptionService, savePath);

        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to initialize Persistence Service. Saving will be disabled.");
            e.printStackTrace();
            persistenceService = null;
        }
    }

    /**
     * Loads existing save data or creates a fresh pet.
     */
    private void initializeModel() {
        // Attempt load
        if (persistenceService != null) {
            try {
                model = persistenceService.load(weatherSystem, clock).orElse(PetFactory.createNewPet("Björni", weatherSystem, clock));
            } catch (Exception e) {
                System.err.println("Warning: Failed to load save file. Starting new game.");
            }
        }
        if (model == null) {
            model = PetFactory.createNewPet("Björni", weatherSystem, clock);
        }
    }

    /**
     * The main game loop handling tick logic.
     */
    private void startGameLoop() {
        lastFrameTime = System.nanoTime();

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculate time passed since last frame (in seconds)
                double deltaSeconds = (now - lastFrameTime) / 1_000_000_000.0;
                lastFrameTime = now;

                // Update game logic
                boolean newDayStarted = clock.tick(deltaSeconds);

                if (newDayStarted) {
                    model.replenishDailyFood();
                }
            }
        };
        gameLoop.start();
    }

    /**
     * Background timer for weather changes.
     */
    private void startWeatherSystem() {
        weatherTimer = new Timeline(new KeyFrame(
                Duration.seconds(WEATHER_CHANGE_INTERVAL),
                event -> {
                    weatherSystem.changeWeather();
                }
        ));
        weatherTimer.setCycleCount(Timeline.INDEFINITE);
        weatherTimer.play();
    }

    /**
     * Configures the JavaFX Stage (Window) and adds exit handlers.
     */
    private void configureStage(Stage stage, Pane root) {
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setWidth(WINDOW_WIDTH);
        stage.setHeight(WINDOW_HEIGHT);

        stage.setOnCloseRequest(event -> {
            // Consume event to prevent immediate close until we say so
            event.consume();
            System.out.println("Exit requested via Window Close...");
            shutdownApplication();
        });

        stage.show();
    }

    /**
     * Centralized shutdown logic to ensure saving happens.
     */
    private void shutdownApplication() {
        stop();
        Platform.exit();
    }

    @Override
    public void stop() {
        if (gameLoop != null) gameLoop.stop();
        if (weatherTimer != null) weatherTimer.stop();
        if (controller != null) {
            try {
                controller.shutdown();
            } catch (Exception e) {
                System.err.println("Error during shutdown save: " + e.getMessage());
            }
        }
    }

    public static void initializeApplication(String[] args) {
        launch(args);
    }
}