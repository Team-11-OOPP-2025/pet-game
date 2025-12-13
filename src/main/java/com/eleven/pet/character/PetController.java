package com.eleven.pet.character;

import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.core.GameException;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.storage.PersistenceService;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller for managing sprites interactions and game logic.
 * Acts as an intermediary between the PetModel and the UI or other systems.
 */
public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private final BooleanProperty inventoryOpenProperty = new SimpleBooleanProperty(false);
    private Timeline autosaveTimer;

    /**
     * Executor for async save operations. Initialized lazily in {@link #initAutosave()}.
     * If this controller is used without calling {@link #initAutosave()}, no executor
     * is created and no resource cleanup is required.
     */
    private ExecutorService saveExecutor;
    private volatile boolean isShutdown = false;

    /**
     * Constructs a PetController with the given model, clock, weather system, and persistence service.
     *
     * @param model       The sprites model to control
     * @param clock       The game clock for time management
     * @param weather     The weather system for environmental effects
     * @param persistence The persistence service for saving/loading
     */
    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
        initControllerLogic();
    }

    /**
     * Watches for state changes to adjust environment timescale.
     * This catches BOTH manual button clicks AND automatic wake-ups.
     */
    private void initControllerLogic() {
        model.getStateProperty().addListener((_, oldState, newState) -> {
            // Pet fell asleep -> Speed up
            if (newState instanceof AsleepState) {
                System.out.println("[Controller] Pet is asleep. Accelerating time.");
                clock.setTimeScale(GameConfig.TIMESCALE_SLEEP);
            }
            // Pet woke up (from sleep) -> Normal speed
            else if (oldState instanceof AsleepState) {
                System.out.println("[Controller] Pet woke up. Normalizing time.");
                clock.setTimeScale(GameConfig.TIMESCALE_NORMAL);
            }
        });
    }

    /**
     * Determines the sprites's emotion based on happiness stats.
     * Centralizes the rules for mood changes.
     */
    public AnimationState calculateEmotion(int happiness) {
        if (happiness >= 80) return AnimationState.VERY_HAPPY;
        if (happiness >= 50) return AnimationState.NEUTRAL;
        if (happiness >= 20) return AnimationState.SAD;
        return AnimationState.VERY_SAD;
    }

    public BooleanProperty inventoryOpenProperty() {
        return inventoryOpenProperty;
    }

    public void setInventoryOpen(boolean isOpen) {
        inventoryOpenProperty.set(isOpen);
    }

    /**
     * Determines if the player is allowed to sleep based on time.
     * Rule: Sleep is allowed between 20:00 (8 PM) and 08:00 (8 AM).
     */
    public boolean isSleepAllowed() {
        return model.shouldPromptSleep();
    }

    /**
     * Handles the feed action by delegating to the model.
     */
    public void handleConsumeAction(Item item) {
        if (model.performConsume(item)) {
            System.out.println("Pet has been fed.");
        } else {
            System.out.println("No food available to feed the sprites.");
        }
    }

    /**
     * Handles the sleep action by delegating to the model after checking for conditions.
     */
    public void handleSleepAction() {
        model.requestSleepInteraction();
    }

    /**
     * Handles the play action by delegating to the model after checking for conditions..
     */
    public void handlePlayAction() {
        // Delegate the task to model which delegates to current state
        // TODO: Update view based on MinigameResult
        model.playRandomMinigame();
    }

    /**
     * Handles the clean action by delegating to the model after checking for conditions.
     */
    public void handleCleanAction() {
        // TODO: Cleaning requires certain conditions are met
        model.performClean();
    }

    /**
     * Toggles the paused state of the game clock.
     */
    public void togglePause() {
        clock.setPaused(!clock.isPaused());
    }

    /**
     * DEBUG: Forces a weather change immediately.
     */
    public void debugChangeWeather() {
        weather.changeWeather();
    }


    /**
     * Initializes and starts the autosave timer. If the timer is already running, this method does nothing.
     */
    public void initAutosave() {
        if (autosaveTimer != null || persistence == null) {
            return;
        }

        // Lazily initialize the executor only when autosave is needed (thread-safe)
        synchronized (this) {
            if (saveExecutor == null) {
                saveExecutor = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "SaveExecutor");
                    t.setDaemon(true);
                    return t;
                });
            }
        }

        autosaveTimer = new Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(GameConfig.AUTOSAVE_INTERVAL_SECONDS),
                        _ -> performAsyncSave("Autosave")
                )
        );
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }

    /**
     * Stops the autosave timer if it is running.
     */
    public void stopAutosave() {
        if (autosaveTimer == null) {
            return;
        }
        autosaveTimer.stop();
        autosaveTimer = null;
    }

    /**
     * Perform an asynchronous save operation.
     *
     * @param reason Reason for the save (for logging purposes)
     */
    private void performAsyncSave(String reason) {
        // Submit save to single-threaded executor to serialize saves and prevent race conditions
        saveExecutor.submit(() -> {
            try {
                System.out.println("Performing async save: " + reason);
                persistence.save(model);
                System.out.println("Game saved (" + reason + ")");
            } catch (GameException e) {
                System.err.println("Error during autosave: " + e.getMessage());
            }
        });
    }

    /**
     * Shutdown the controller, ensuring all resources are cleaned up and
     * a final save is performed.
     */
    public void shutdown() {
        if (isShutdown) {
            return;
        }
        isShutdown = true;

        stopAutosave();

        // Shutdown the executor and wait for pending saves to complete if it exists
        if (saveExecutor != null) {
            saveExecutor.shutdown();
            try {
                if (!saveExecutor.awaitTermination(GameConfig.SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.err.println("Save executor did not terminate in time, forcing shutdown");
                    saveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for save executor shutdown");
                saveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        if (persistence != null) {
            try {
                System.out.println("Performing synchronous save: Shutdown Save");
                persistence.save(model);
                System.out.println("Game saved (Shutdown Save)");
            } catch (GameException e) {
                System.err.println("Error during shutdown save: " + e.getMessage());
            }
        } else {
            System.err.println("Cannot save game on shutdown: persistence is not initialized.");
        }
    }

    /**
     * Returns the Game Pane for the TV to display.
     * This replaces the old logic of opening a new Window.
     */
    public Pane getMinigamePane() {
        // TODO: Implement actual minigame pane retrieval
        // This should call the MiniGameController to get the actual game pane
        // and then call model with the result of the minigame.
        StackPane placeholder = new StackPane();
        placeholder.setStyle("-fx-background-color: black; -fx-border-color: #444; -fx-border-width: 2;");

        Label label = new Label("Woops... You killed the Pet just now! CRY ABOUT IT NOW HA HA HA HA!");
        label.setStyle("-fx-text-fill: #00ff00; -fx-font-family: 'Monospaced'; -fx-font-weight: bold; -fx-text-alignment: center; -fx-font-size: 14px;");

        placeholder.getChildren().add(label);
        return placeholder;
    }
}
