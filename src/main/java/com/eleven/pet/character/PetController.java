package com.eleven.pet.character;

import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.core.GameException;
import com.eleven.pet.daily_reward.Chest;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.MiniGameController;
import com.eleven.pet.minigames.ui.MiniGameView;
import com.eleven.pet.storage.PersistenceService;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the virtual pet.
 * <p>
 * Orchestrates interactions between the {@link PetModel} and external game
 * systems such as the clock, weather, persistence, inventory and minigames.
 * Provides high–level operations that the UI layer can invoke.
 */
public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private final BooleanProperty inventoryOpenProperty = new SimpleBooleanProperty(false);
    private Timeline autosaveTimer;
    private ExecutorService saveExecutor;
    private volatile boolean isShutdown = false;

    /**
     * Creates a new {@code PetController}.
     *
     * @param model       the underlying {@link PetModel} representing the pet state
     * @param clock       the {@link GameClock} used for time scaling and pausing
     * @param weather     the {@link WeatherSystem} used to change in-game weather
     * @param persistence the {@link PersistenceService} used for saving the game state
     */
    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
        initControllerLogic();
    }

    private void initControllerLogic() {
        model.getStateProperty().addListener((_, oldState, newState) -> {
            if (newState instanceof AsleepState) {
                clock.setTimeScale(GameConfig.TIMESCALE_SLEEP);
            }
            else if (oldState instanceof AsleepState) {
                clock.setTimeScale(GameConfig.TIMESCALE_NORMAL);
            }
        });
    }

    /**
     * Determines the {@link AnimationState} to use based on the pet's happiness.
     *
     * @param happiness numeric happiness value (typically 0–100)
     * @return the corresponding {@link AnimationState} for the given happiness
     */
    public AnimationState calculateEmotion(int happiness) {
        if (happiness >= 80) return AnimationState.VERY_HAPPY;
        if (happiness >= 50) return AnimationState.NEUTRAL;
        if (happiness >= 20) return AnimationState.SAD;
        return AnimationState.VERY_SAD;
    }

    /**
     * Property indicating whether the inventory UI is currently open.
     *
     * @return a {@link BooleanProperty} that is true when the inventory is open
     */
    public BooleanProperty inventoryOpenProperty() { return inventoryOpenProperty; }

    /**
     * Sets whether the inventory UI is currently open.
     *
     * @param isOpen {@code true} to mark the inventory as open, {@code false} otherwise
     */
    public void setInventoryOpen(boolean isOpen) { inventoryOpenProperty.set(isOpen); }

    /**
     * Checks whether the pet is allowed to sleep and the user should be prompted.
     *
     * @return {@code true} if a sleep interaction should be offered, {@code false} otherwise
     */
    public boolean isSleepAllowed() { return model.shouldPromptSleep(); }

    /**
     * Handles consumption of an item by the pet.
     *
     * @param item the {@link Item} to consume
     */
    public void handleConsumeAction(Item item) {
        if (model.performConsume(item)) System.out.println("Pet has been fed.");
        else System.out.println("No food available.");
    }

    /**
     * Triggers a sleep interaction request for the pet.
     */
    public void handleSleepAction() { model.requestSleepInteraction(); }

    /**
     * Starts a random minigame for the pet to play.
     */
    public void handlePlayAction() { model.playRandomMinigame(); }

    /**
     * Performs a cleaning action on the pet or its environment.
     */
    public void handleCleanAction() { model.performClean(); }

    /**
     * Toggles the paused state of the game clock.
     */
    public void togglePause() { clock.setPaused(!clock.isPaused()); }

    /**
     * Cycles the in-game weather to a different state (debug functionality).
     */
    public void debugChangeWeather() { weather.changeWeather(); }
    
    /**
     * Checks if the daily reward is currently available to be claimed.
     *
     * @return {@code true} if the reward cooldown has elapsed, {@code false} otherwise
     */
    public boolean isDailyRewardAvailable() { return model.getRewardCooldown() <= 0; }

    /**
     * Generates a list of daily reward chest options for the player to choose from.
     *
     * @return a list of newly created {@link Chest} instances
     */
    public List<Chest> generateDailyRewardOptions() {
        List<Chest> chests = new ArrayList<>();
        for (int i = 0; i < 5; i++) chests.add(new Chest());
        return chests;
    }

    /**
     * Claims the specified daily reward chest if it is available.
     * <p>
     * Opens the chest, applies its rewards to the model and resets the cooldown.
     *
     * @param chest the {@link Chest} selected by the player
     */
    public void claimDailyReward(Chest chest) {
        if (isDailyRewardAvailable()) {
            chest.open(model);
            model.setRewardCooldown(GameConfig.DAILY_REWARD_COOLDOWN);
            System.out.println("Daily reward claimed.");
        }
    }

    /**
     * Initializes periodic autosaving of the game state.
     * <p>
     * Creates a background executor and JavaFX {@link Timeline} that saves the
     * {@link PetModel} at fixed intervals defined by
     * {@link GameConfig#AUTOSAVE_INTERVAL_SECONDS}.
     * Does nothing if autosave has already been initialized or persistence is unavailable.
     */
    public void initAutosave() {
        if (autosaveTimer != null || persistence == null) return;
        synchronized (this) {
            if (saveExecutor == null) {
                saveExecutor = Executors.newSingleThreadExecutor(r -> {
                    Thread t = new Thread(r, "SaveExecutor");
                    t.setDaemon(true);
                    return t;
                });
            }
        }
        autosaveTimer = new Timeline(new javafx.animation.KeyFrame(
                javafx.util.Duration.seconds(GameConfig.AUTOSAVE_INTERVAL_SECONDS),
                _ -> performAsyncSave("Autosave")
        ));
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }

    /**
     * Stops the autosave timer if it is running.
     * <p>
     * Does not shut down the underlying executor.
     */
    public void stopAutosave() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
            autosaveTimer = null;
        }
    }

    private void performAsyncSave(String reason) {
        saveExecutor.submit(() -> {
            try {
                persistence.save(model);
                System.out.println("Game saved (" + reason + ")");
            } catch (GameException e) {
                System.err.println("Error during autosave: " + e.getMessage());
            }
        });
    }

    /**
     * Performs a graceful shutdown sequence for this controller.
     * <p>
     * Stops autosave, shuts down the save executor and performs a final
     * synchronous save via {@link PersistenceService}.
     */
    public void shutdown() {
        if (isShutdown) return;
        isShutdown = true;
        stopAutosave();
        if (saveExecutor != null) {
            saveExecutor.shutdown();
            try {
                if (!saveExecutor.awaitTermination(5, TimeUnit.SECONDS)) saveExecutor.shutdownNow();
            } catch (InterruptedException e) {
                saveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        if (persistence != null) {
            try {
                persistence.save(model);
            } catch (GameException e) {
                System.err.println("Error during shutdown save: " + e.getMessage());
            }
        }
    }

    /**
     * Returns the Minigame Pane to display inside the TV.
     * <p>
     * Initializes a {@link MiniGameView} and its controller.
     *
     * @param onExit callback to run when the game finishes (zooms out)
     * @return the root {@link Pane} of the minigame view
     */
    public Pane getMinigamePane(Runnable onExit) {
        MiniGameView gameView = new MiniGameView();
        // The controller binds itself to the view and model and starts the logic
        new MiniGameController(gameView, model, onExit);
        return gameView;
    }
}