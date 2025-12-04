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

public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private Timeline autosaveTimer;
    private long lastUpdateTime = 0;

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
        if (autosaveTimer != null)
            return;

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
        // Run save in a separate thread to avoid blocking UI
        Thread saveThread = new Thread(() -> {
            try {
                System.out.println("Performing async save: " + reason);
                persistence.save(model);
                Platform.runLater(() -> System.out.println("Game saved (" + reason + ")"));
            } catch (GameException e) {
                System.err.println("Error during autosave: " + e.getMessage());
            }
        }, "AutoSaveThread");
        saveThread.setDaemon(true);
        saveThread.start();
    }

    public void shutdown() {
        stopAutosave();
        try {
            System.out.println("Performing async save: " + "Shutdown Save");
            persistence.save(model);
            System.out.println("Game saved (" + "Shutdown Save" + ")");
        } catch (GameException e) {
            System.err.println("Error during autosave: " + e.getMessage());
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
