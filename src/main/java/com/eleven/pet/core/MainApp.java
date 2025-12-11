package com.eleven.pet.core;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetView;
import com.eleven.pet.character.behavior.PetDefinition;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.storage.GcmEncryptionService;
import com.eleven.pet.storage.KeyLoader;
import com.eleven.pet.storage.PersistenceService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.crypto.SecretKey;

/**
 * Main application class for the virtual sprites game "Björni".
 * Initializes and starts the game, including loading assets, setting up the UI,
 * and managing the game loop.
 */
public class MainApp extends Application {
    private final GameClock clock = new GameClock();
    private final WeatherSystem weatherSystem = new WeatherSystem();

    private PetController controller;
    private GameEngine gameEngine;
    private PersistenceService persistenceService;
    private PetDefinition petDefinition;
    /**
     * Application entry point.
     *
     * @param primaryStage the primary stage for this application
     */
    @Override
    public void start(Stage primaryStage) {
        AssetLoader.getInstance().loadAll();

        initializePersistence();
        PetModel model = loadOrCreatePet();

        gameEngine = new GameEngine(model, clock, weatherSystem);

        controller = new PetController(model, clock, weatherSystem, persistenceService);
        PetView view = new PetView(model, controller, clock, weatherSystem);
        

        controller.initAutosave();
        gameEngine.start();

        configureStage(primaryStage, view.initializeUI());
    }

    /**
     * Initializes the persistence service with encryption.
     * Falls back to disabling persistence if initialization fails.
     */
    private void initializePersistence() {
        try {
            SecretKey key;
            try {
                key = KeyLoader.loadKey();
            } catch (Exception e) {
                System.out.println("Generating new development key...");
                key = KeyLoader.generateDevKey();
            }
            persistenceService = new PersistenceService(new GcmEncryptionService(key), GameConfig.SAVE_PATH);
        } catch (Exception e) {
            System.err.println("CRITICAL: Persistence init failed. Saving disabled.");
            persistenceService = null;
        }
    }

    /**
     * Loads the sprites from persistence or creates a new one if loading fails.
     *
     * @return the loaded or newly created PetModel
     */
    private PetModel loadOrCreatePet() {
        if (persistenceService != null) {
            try {
                // TODO: Allow user to choose Pet name on first launch in future
                return persistenceService.load(weatherSystem, clock)
                        .orElseGet(() -> PetFactory.createNewPet("Björni", weatherSystem, clock));
            } catch (Exception e) {
                System.err.println("Save file corrupted or version mismatch. Creating new.");
            }
        }
        // TODO: Allow user to choose name on first launch
        return PetFactory.createNewPet("Björni", weatherSystem, clock);
    }

    /**
     * Configures and shows the main application stage.
     *
     * @param stage the primary stage
     * @param root  the root pane of the scene
     */
    private void configureStage(Stage stage, Pane root) {
        Scene scene = new Scene(root, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        stage.setTitle(GameConfig.APP_TITLE);
        stage.setScene(scene);
        stage.setResizable(false);

        // Handle "X" button click
        stage.setOnCloseRequest(event -> {
            event.consume();
            shutdown();
        });

        stage.show();
    }

    /**
     * Clean shutdown sequence
     */
    private void shutdown() {
        System.out.println("Shutting down...");

        if (gameEngine != null) gameEngine.stop();

        if (controller != null) {
            try {
                controller.shutdown(); // Saves game
            } catch (Exception e) {
                System.err.println("Save on exit failed: " + e.getMessage());
            }
        }

        Platform.exit();
        System.exit(0);
    }

    public static void initializeApplication(String[] args) {
        launch(args);
    }
}