package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.clock.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import jdk.jfr.FlightRecorder;

import java.util.Objects;

/**
 * Main domain entity representing the pet.
 */
public class PetModel {

    private final WeatherSystem weatherSystem;
    private final GameClock clock;      // TODO: define GameClock class/package if not existing

    // Sleep tracking
    private long sleepStartTime;
    private boolean sleptThisNight;

    // Core components
    private final Inventory inventory;  // TODO: ensure Inventory class exists
    private final PetStats stats;

    public PetModel(WeatherSystem weatherSystem, GameClock clock) {
        this.weatherSystem = Objects.requireNonNull(weatherSystem, "weatherSystem");
        this.clock = Objects.requireNonNull(clock, "clock");

        this.inventory = new Inventory();      // empty inventory
        this.stats = new PetStats();           // you can seed defaults in factory
        this.sleepStartTime = 0L;
        this.sleptThisNight = false;
    }

    public GameClock getClock() {
        return clock;
    }

    
    public void sleep() {
        if (currentState.get() != null) {
            currentState.get().handleSleep(this);
        }
    }
    
    // Minigame system
    public boolean canPlayMinigame() {
        // TODO: Implement minigame eligibility check
        return false;
    }
    
    public MinigameResult playMinigame(Minigame minigame) {
        // TODO: Implement minigame play logic
        return null;
    }
    
    // Daily management
    public void replenishDailyFood() {
        int foodToAdd = 3 + random.nextInt(6); // Random 3-8 (3 + 0-5)
        int newFoodCount = foodCount.get() + foodToAdd;
        foodCount.set(newFoodCount);
        System.out.println(name + " received daily food replenishment: +" + foodToAdd + " food! Total: " + foodCount.get());
    }
    
    public boolean shouldPromptSleep() {
        // TODO: Implement sleep prompt logic
        return false;
    }
    
    // Environment listeners
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this);
        }
    }
    
    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction (modify happiness based on weather)
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public PetStats getStats() {
        return stats;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }
    
    public GameClock getGameClock() {
        return clock;
    }
    
    public int getFoodCount() {
        return foodCount.get();
    }
    
    public IntegerProperty getFoodCountProperty() {
        return foodCount;
    }
    
    public void setFoodCount(int val) {
        foodCount.set(val);
    }
    
    public boolean getSleptThisNight() {
        return sleptThisNight;
    }
    
    public void setSleptThisNight(boolean slept) {
        this.sleptThisNight = slept;
    }
    
    public long getSleepStartTime() {
        return sleepStartTime;
    }
    
    public void setSleepStartTime(long timestamp) {
        this.sleepStartTime = timestamp;
    }
}
