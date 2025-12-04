package com.eleven.pet.controller;

import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.Minigame;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.service.persistence.GameException;
import com.eleven.pet.service.persistence.PersistenceService;
import com.eleven.pet.view.MiniGameView;
import javafx.animation.Timeline;
import javafx.application.Platform;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private Timeline autosaveTimer;
    private long lastUpdateTime = 0;
    /**
     * Executor for async save operations. Initialized lazily in {@link #initAutosave()}.
     * If this controller is used without calling {@link #initAutosave()}, no executor
     * is created and no resource cleanup is required.
     */
    private ExecutorService saveExecutor;
    private volatile boolean isShutdown = false;

    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
    }

    public void handleFeedAction() {
        model.performConsume(ItemRegistry.get(0));
    }

    public void handleSleepAction() {
        model.sleep();
    }

    public void handleSleepButton() {
        // Called when player clicks the sleep button during sleep hours
        // Switch to asleep state which will apply sleep rewards in onEnter
        model.performSleep();

        // Jump time to 8:00 AM (8/24 = 0.3333... of the day)
        if (clock != null) {
            double targetTime = (8.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
            // Set the game time directly by calculating the difference
            double currentTime = clock.getGameTime();
            double timeDelta;

            if (currentTime > targetTime) {
                // Past midnight, need to go to next day's 8 AM
                timeDelta = (GameConfig.DAY_LENGTH_SECONDS - currentTime) + targetTime;
            } else {
                // Before 8 AM, jump to 8 AM
                timeDelta = targetTime - currentTime;
            }

            // Advance time by the delta
            clock.tick(timeDelta);
        }

        // Wake up - switch back to awake state
        StateRegistry registry = StateRegistry.getInstance();
        model.changeState(registry.getState("awake"));
    }

    public void handlePlayAction() {
        // Play a random minigame
        model.playRandomMinigame();
    }

    public void handlePlayMinigame(Minigame minigame) {
        model.playMinigame(minigame);
    }

    public void handleCleanAction() {
        model.clean();
    }

    public void togglePause() {
        // TODO: Implement pause/resume functionality
    }

    public void debugChangeWeather() {
        // TODO: Implement debug weather change
    }

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
                        event -> performAsyncSave("Autosave")
                )
        );
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
    }

    public void stopAutosave() {
        if (autosaveTimer == null) {
            return;
        }
        autosaveTimer.stop();
        autosaveTimer = null;
    }

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

    public void shutdown() {
        // Make shutdown idempotent - only perform shutdown operations once
        if (isShutdown) {
            return;
        }
        isShutdown = true;
        
        stopAutosave();
        
        // Shutdown the executor and wait for pending saves to complete (only if it was initialized)
        if (saveExecutor != null) {
            saveExecutor.shutdown();
            try {
                // Wait for pending saves to complete
                if (!saveExecutor.awaitTermination(GameConfig.SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    System.err.println("Save executor did not terminate in time, forcing shutdown");
                    saveExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for save executor shutdown");
                saveExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while waiting for save executor shutdown");
            saveExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        } finally {
            // Perform final synchronous save on shutdown after executor has terminated
            // to ensure no concurrent saves occur
            try {
                if (persistence != null) {
                    System.out.println("Performing synchronous save: Shutdown Save");
                    persistence.save(model);
                    System.out.println("Game saved (Shutdown Save)");
                } else {
                    System.err.println("Cannot save game on shutdown: persistence is not initialized.");
                }
            } catch (GameException e) {
                System.err.println("Error during shutdown save: " + e.getMessage());
            }
        }
    }

    // Legacy methods for backward compatibility
    public void handleClean() {
        handleCleanAction();
    }

    public void handleFeed() {
        handleFeedAction();
    }

    public void handlePlay() {
        // Create Swing minigame in a separate window
        MiniGameView.showMiniGame(model);
    }

    public void handleSleep() {
        handleSleepAction();
    }
}
