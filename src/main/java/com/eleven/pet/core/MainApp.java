package com.eleven.pet.core;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetView;
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
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainApp extends Application {
    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;
    private static final String APP_TITLE = "Björni";
    private static final Path SAVE_PATH = Paths.get("savegame.dat");

    private final GameClock clock = new GameClock();
    private final WeatherSystem weatherSystem = new WeatherSystem();

    private PetController controller;
    private GameEngine gameEngine;
    private PersistenceService persistenceService;

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

    private void initializePersistence() {
        try {
            SecretKey key;
            try {
                key = KeyLoader.loadKey();
            } catch (Exception e) {
                System.out.println("Generating new development key...");
                key = KeyLoader.generateDevKey();
            }
            persistenceService = new PersistenceService(new GcmEncryptionService(key), SAVE_PATH);
        } catch (Exception e) {
            System.err.println("CRITICAL: Persistence init failed. Saving disabled.");
            persistenceService = null;
        }
    }

    private PetModel loadOrCreatePet() {
        if (persistenceService != null) {
            try {
                return persistenceService.load(weatherSystem, clock)
                        .orElseGet(() -> PetFactory.createNewPet("Björni", weatherSystem, clock));
            } catch (Exception e) {
                System.err.println("Save file corrupted or version mismatch. Creating new.");
            }
        }
        return PetFactory.createNewPet("Björni", weatherSystem, clock);
    }

    private void configureStage(Stage stage, Pane root) {
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        stage.setTitle(APP_TITLE);
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