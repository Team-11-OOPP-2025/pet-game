package com.eleven.pet.controller;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.FoodItem;
import com.eleven.pet.model.Minigame;
import com.eleven.pet.model.MinigameResult;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.persistence.PersistenceService;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

public class PetController {
    private final PetModel model;
    private final GameClock clock;
    private final WeatherSystem weather;
    private final PersistenceService persistence;
    private Timeline autosaveTimer;
    
    public PetController(PetModel model, GameClock clock, WeatherSystem weather, PersistenceService persistence) {
        this.model = model;
        this.clock = clock;
        this.weather = weather;
        this.persistence = persistence;
    }
    
    public void handleFeedAction() {
        // Use the basic food from inventory
        FoodItem basicFood = new FoodItem("Basic Food", 20);
        if (!model.consume(basicFood)) {
            System.out.println("Cannot feed pet right now or no food available!");
        }
    }
    
    public void handleSleepAction() {
        model.performSleep();
    }
    
    public void handlePlayAction() {
        if (model.getCurrentState() != null) {
            model.getCurrentState().handlePlay(model);
        }
    }
    
    public void handlePlayMinigame(Minigame minigame) {
        MinigameResult result = model.playMinigame(minigame);
        System.out.println("Minigame result: " + result.getMessage());
    }
    
    public void handleCleanAction() {
        model.performClean();
    }
    
    public void togglePause() {
        if (clock != null) {
            clock.setPaused(!clock.isPaused());
        }
    }
    
    public void debugChangeWeather() {
        if (weather != null) {
            weather.changeWeather();
        }
    }
    
    public void initAutosave() {
        if (persistence == null) {
            return;
        }
        
        autosaveTimer = new Timeline(new KeyFrame(
            Duration.seconds(GameConfig.AUTOSAVE_INTERVAL_SECONDS),
            event -> performAsyncSave("autosave")
        ));
        autosaveTimer.setCycleCount(Timeline.INDEFINITE);
        autosaveTimer.play();
        
        System.out.println("✓ Autosave initialized (every " + GameConfig.AUTOSAVE_INTERVAL_SECONDS + "s)");
    }
    
    public void stopAutosave() {
        if (autosaveTimer != null) {
            autosaveTimer.stop();
        }
    }
    
    private void performAsyncSave(String reason) {
        new Thread(() -> {
            try {
                if (persistence != null) {
                    persistence.save(model);
                    Platform.runLater(() -> System.out.println("💾 Game saved (" + reason + ")"));
                }
            } catch (Exception e) {
                Platform.runLater(() -> System.err.println("❌ Save failed: " + e.getMessage()));
            }
        }).start();
    }
    
    public void shutdown() {
        stopAutosave();
        performAsyncSave("shutdown");
    }
}
