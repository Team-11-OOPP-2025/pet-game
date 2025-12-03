package com.eleven.pet.controller;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.Minigame;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.FoodItem;
import com.eleven.pet.persistence.PersistenceService;
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
        model.performConsume(new FoodItem("Food", GameConfig.FEED_HUNGER_RESTORE));
    }

    public void handleSleepAction() {
        model.sleep();
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
        model.playRandomMinigame();
    }

    public void handleSleep() {
        handleSleepAction();
    }
}
