package com.eleven.pet.character;

import com.eleven.pet.core.GameConfig;
import com.eleven.pet.core.GameException;
import com.eleven.pet.daily_reward.Chest;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.GameSession;
import com.eleven.pet.minigames.MiniGameController;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.ui.MiniGameView;
import com.eleven.pet.network.leaderboard.LeaderboardService;
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
 * </p>
 */
public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private final LeaderboardService leaderboard;

    private final BooleanProperty inventoryOpenProperty = new SimpleBooleanProperty(false);
    private Timeline autosaveTimer;
    private ExecutorService saveExecutor;
    private volatile boolean isShutdown = false;

    /**
     * Creates a new {@code PetController}.
     *
     * @param model              the underlying {@link PetModel} representing the pet state
     * @param clock              the {@link GameClock} used for time scaling and pausing
     * @param weather            the {@link WeatherSystem} used to change in-game weather
     * @param persistence        the {@link PersistenceService} used for saving the game state
     * @param leaderboardService the {@link LeaderboardService} used for submitting scores
     */
    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence, LeaderboardService leaderboardService) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
        this.leaderboard = leaderboardService;

        initControllerLogic();
    }

    private void initControllerLogic() {
        model.getStateProperty().addListener((_, _, newState) -> {
            if (newState != null) {
                clock.setTimeScale(newState.getTimeScale());
            }
        });

        // Initialize Player Registration
        if (model.getPlayerId() != null && model.getSecretKey() != null) {
            // Scenario A: Player already registered (loaded from save file)
            leaderboard.setCredentials(model.getPlayerId(), model.getSecretKey());
            System.out.println("Restored leaderboard credentials for Player ID: " + model.getPlayerId());
        } else {
            // Scenario B: New Player. Register async, update model, then save.
            leaderboard.registerPlayer().thenAccept(registration -> {
                // 1. Update Model
                synchronized (model) {
                    model.setPlayerId(registration.getPlayerId());
                    model.setSecretKey(registration.getSecretKey());
                }

                // 2. Activate Client
                leaderboard.setCredentials(registration.getPlayerId(), registration.getSecretKey());

                // 3. Persist Credentials to disk
                if (persistence != null) {
                    try {
                        persistence.save(model);
                        System.out.println("Registered and persisted new player credentials.");
                    } catch (GameException e) {
                        System.err.println("Failed to persist player credentials: " + e.getMessage());
                    }
                }
            }).exceptionally(e -> {
                System.err.println("Failed to register player: " + e.getMessage());
                return null;
            });
        }
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
    public BooleanProperty inventoryOpenProperty() {
        return inventoryOpenProperty;
    }

    /**
     * Sets whether the inventory UI is currently open.
     *
     * @param isOpen {@code true} to mark the inventory as open, {@code false} otherwise
     */
    public void setInventoryOpen(boolean isOpen) {
        inventoryOpenProperty.set(isOpen);
    }

    /**
     * Checks whether the pet is allowed to sleep and the user should be prompted.
     *
     * @return {@code true} if a sleep interaction should be offered, {@code false} otherwise
     */
    public boolean isSleepAllowed() {
        return model.shouldPromptSleep();
    }

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
    public void handleSleepAction() {
        model.requestSleepInteraction();
    }

    /**
     * Performs a cleaning action on the pet or its environment.
     */
    public void handleCleanAction() {
        model.performClean();
    }

    /**
     * Toggles the paused state of the game clock.
     */
    public void togglePause() {
        clock.setPaused(!clock.isPaused());
    }

    /**
     * Cycles the in-game weather to a different state (debug functionality).
     */
    public void debugChangeWeather() {
        weather.changeWeather();
    }

    /**
     * Checks if the daily reward is currently available to be claimed.
     *
     * @return {@code true} if the reward cooldown has elapsed, {@code false} otherwise
     */
    public boolean isDailyRewardAvailable() {
        return model.getRewardCooldown() <= 0;
    }

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
     * </p>
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

    public boolean canPlayMinigame() {
        return model.canPlayMinigame();
    }

    /**
     * Initializes periodic autosaving of the game state.
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
     */
    public void stopAutosave() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
            autosaveTimer = null;
        }
    }

    /**
     * Perform an asynchronous save operation.
     */
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
     *
     * @return the root {@link Pane} of the minigame view, or null if no games available
     */
    public Pane getMinigamePane(Runnable onUIExit) {
        Minigame gameFactory = new MiniGameController().startRandomGame();
        if (gameFactory == null) {
            System.out.println("No minigames available.");
            onUIExit.run();
            return null;
        }

        GameSession session = gameFactory.createSession();

        session.start(result -> {
            model.applyMinigameResult(result);
            if (leaderboard != null)
                leaderboard.submitScore(model.getName(), result);
            if (onUIExit != null) onUIExit.run();
        });

        return new MiniGameView(session.getView());
    }

    /**
     * Marks the tutorial as completed and performs related actions.
     */
    public void completeTutorial() {
        model.setTutorialCompleted(true);
        // Force a save immediately
        try {
            if (persistence != null) {
                persistence.save(model);
            }
        } catch (Exception e) {
            System.err.println("Failed to save tutorial status: " + e.getMessage());
        }

        // Unpause the game after tutorial finishes and move forward half a day
        togglePause();
        clock.tick(GameConfig.DAY_LENGTH_SECONDS / 2);
    }

    /**
     * Initializes tutorial-specific logic, such as advancing time for certain prompts.
     */
    public void initTutorialLogic() {
        // Advance time by half a day to test tutorial steps that depend on time (Sleep prompt)
        clock.tick(GameConfig.DAY_LENGTH_SECONDS / 2);
        togglePause();
    }

    /**
     * Exposes the leaderboard service for UI components.
     *
     * @return the active leaderboard service
     */
    public LeaderboardService getLeaderboardService() {
        return leaderboard;
    }
}