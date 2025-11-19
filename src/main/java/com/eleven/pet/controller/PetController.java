package com.eleven.pet.controller;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.persistence.PersistenceService;

import javafx.animation.AnimationTimer;

public class PetController {
    private final PetModel model;
    private final PersistenceService persistence;
    private long lastUpdateTime = 0;
    
    public PetController(PetModel model, PersistenceService pService) {
        this.model = model;
        this.persistence = pService;
    }
    
    public void handleClean() {
        model.clean();
    }
    
    public void handleFeed() {
        model.feed();
    }
    
    public void handlePlay() {
        model.play();
    }
    
    public void handleSleep() {
        model.sleep();
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
                if (model.getGameClock() != null) {
                    boolean newDayStarted = model.getGameClock().tick(elapsedSeconds);
                    
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
