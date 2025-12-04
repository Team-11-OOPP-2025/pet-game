package com.eleven.pet.controller;

import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.Minigame;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.service.persistence.PersistenceService;
import com.eleven.pet.view.MiniGameView;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;

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
        // TODO: Implement autosave initialization
    }

    public void stopAutosave() {
        // TODO: Implement autosave stop
    }

    private void performAsyncSave(String reason) {
        // TODO: Implement async save
    }

    public void shutdown() {
        // TODO: Implement shutdown (stop autosave, final save)
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

    /**
     * Starts the main game loop that updates the GameClock and model.
     */
    public void startGameLoop() {
        AnimationTimer gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                double elapsedSeconds = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                if (clock != null) {
                    clock.tick(elapsedSeconds);
                }
            }
        };

        gameLoop.start();
        System.out.println("✓ Game loop started!");
    }
}
