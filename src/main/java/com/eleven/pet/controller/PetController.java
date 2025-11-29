package com.eleven.pet.controller;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.Minigame;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.service.persistence.PersistenceService;
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
        model.feed();
    }
    
    public void handleSleepAction() {
        model.sleep();
    }
    
    public void handlePlayAction() {
        model.play();
    }
    
    public void handlePlayMinigame(Minigame minigame) {
        // TODO: Implement minigame handling
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
        handlePlayAction();
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
                
                // Calculate elapsed time in seconds
                double elapsedSeconds = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                
                // Update the game clock
                if (clock != null) {
                    boolean newDayStarted = clock.tick(elapsedSeconds);
                    
                    if (newDayStarted) {
                        model.replenishDailyFood();
                    }
                }
            }
        };
        
        gameLoop.start();
        System.out.println("✓ Game loop started!");
    }
}
