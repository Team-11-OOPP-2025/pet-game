package com.eleven.pet.controller;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.persistence.PersistenceService;

import javafx.animation.AnimationTimer;

public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private long lastUpdateTime = 0;
    
    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
    }
    
    public void handleFeedAction() {
        model.performFeed();
    }
    
    public void handleSleepAction() {
        model.performSleep();
    }
    
    public void handlePlayAction() {
        model.performPlay();
    }
    
    public void handleCleanAction() {
        model.performClean();
    }
    
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
