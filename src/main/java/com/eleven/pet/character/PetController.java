package com.eleven.pet.character;

import com.eleven.pet.core.GameConfig;
import com.eleven.pet.core.GameException;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.storage.PersistenceService;
import javafx.animation.Timeline;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private Timeline autosaveTimer;

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
        // Delegates the consumption to the current state
        // TODO: Use actual food item from inventory instead of placeholder
        // Achieve that through accepting a Item parameter in this method and passing it down
        if (model.performConsume(ItemRegistry.get(0))) {
            System.out.println("Pet has been fed.");
        } else {
            System.out.println("No food available to feed the pet.");
        }
    }

    public void handleSleepAction() {
        // Called when player clicks the sleep button during sleep hours
        // Switch to asleep state which will apply sleep rewards in onEnter
        model.performSleep();
    }

    public void handlePlayAction() {
        // Delegate the task to model which delegates to current state
        model.playRandomMinigame();
    }

    public void handleCleanAction() {
        // TODO: Cleaning requires certain conditions are met
        model.performClean();
    }

    public void togglePause() {
        clock.setPaused(!clock.isPaused());
    }

    public void debugChangeWeather() {
        weather.changeWeather();
    }


    // Save Management
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
}
