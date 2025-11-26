package com.eleven.pet.model;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.time.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.state.AwakeState;
import com.eleven.pet.state.PetState;
import com.eleven.pet.state.StateRegistry;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class PetModel implements TimeListener, WeatherListener {
    private final String name;
    private final PetStats stats;
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private final Inventory inventory;
    private boolean sleptThisNight;
    private long sleepStartTime;
    
    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.stats = new PetStats(0, 100);
        this.currentState = new SimpleObjectProperty<>();
        this.inventory = new Inventory();
        this.sleptThisNight = false;
        this.sleepStartTime = 0;
        
        // Initialize with some food
        FoodItem basicFood = new FoodItem("Basic Food", 20);
        inventory.add(basicFood, 50);
        
        // Start in awake state (create directly to avoid ServiceLoader issues)
        currentState.set(new AwakeState());
        System.out.println(name + " initialized in: " + currentState.get().getStateName());
    }
    
    public void changeState(PetState newState) {
        if (newState != null) {
            currentState.set(newState);
            System.out.println(name + " changed state to: " + newState.getStateName());
        }
    }
    
    public boolean consume(Consumable item) {
        if (inventory.remove(item, 1)) {
            return currentState.get().handleConsume(this, item);
        }
        return false;
    }
    
    public void performSleep() {
        if (currentState.get() != null) {
            currentState.get().handleSleep(this);
        }
    }
    
    public void wakeUp() {
        currentState.set(new AwakeState());
        setSleptThisNight(true);
        System.out.println(name + " woke up refreshed!");
    }
    
    public void performClean() {
        if (currentState.get() != null) {
            currentState.get().handleClean(this);
        }
    }
    
    public void replenishDailyFood() {
        FoodItem basicFood = new FoodItem("Basic Food", 20);
        int newFood = 3 + (int)(Math.random() * 6);
        inventory.add(basicFood, newFood);
        System.out.println("Daily food replenished: +" + newFood + " food");
        
        // Reset sleep flag for new day
        sleptThisNight = false;
    }
    
    public boolean shouldPromptSleep() {
        return !sleptThisNight && 
               clock.getCycle() == com.eleven.pet.environment.time.DayCycle.NIGHT &&
               stats.getStat(PetStats.STAT_ENERGY).get() <= com.eleven.pet.config.GameConfig.SLEEP_ENERGY_THRESHOLD;
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
    
    public boolean canPlayMinigame() {
        return currentState.get().getStateName().equals(AwakeState.STATE_NAME) &&
               stats.getStat(PetStats.STAT_ENERGY).get() >= 20;
    }
    
    public MinigameResult playMinigame(Minigame minigame) {
        if (!canPlayMinigame()) {
            return new MinigameResult(false, 0, "Pet is too tired to play!");
        }
        
        MinigameResult result = minigame.play(this);
        stats.modifyStat(PetStats.STAT_HAPPINESS, result.getHappinessDelta());
        stats.modifyStat(PetStats.STAT_ENERGY, -10);
        
        return result;
    }
    
    public Inventory getInventory() {
        return inventory;
    }
    
    public PetStats getStats() {
        return stats;
    }
    
    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }
    
    public PetState getCurrentState() {
        return currentState.get();
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this);
        }
    }
    
    @Override
    public void onWeatherChanged(WeatherState newWeather) {
        if (newWeather != null) {
            double happinessMod = newWeather.getHappinessModifier();
            stats.modifyStat(PetStats.STAT_HAPPINESS, (int)(happinessMod * 10));
        }
    }
}
