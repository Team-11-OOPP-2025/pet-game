package com.eleven.pet.model;

import java.util.Random;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.time.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.state.PetState;

import javafx.beans.property.IntegerProperty;       //new
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;    //new
import javafx.beans.property.SimpleObjectProperty;

public class PetModel implements TimeListener, WeatherListener {
    private final String name;
    private final PetStats stats;
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private final IntegerProperty foodCount;          //new
    private final Random random;              //new
    
    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.stats = new PetStats(0, 100);
        this.currentState = new SimpleObjectProperty<>();
        this.foodCount = new SimpleIntegerProperty(50);     //new
        this.random = new Random();             //new
    }
    
    public void changeState(PetState newState) {
    }
    
    public void performFeed() {
        if (foodCount.get() > 0) {
            foodCount.set(foodCount.get() - 1);
            stats.modifyStat(PetStats.STAT_HUNGER, 20); // Increase hunger by 20
            System.out.println("Fed pet! Remaining food: " + foodCount.get());
        } else {
            System.out.println("No food left!");
        }
    }
    
    public void replenishDailyFood() {
        int newFood = 3 + random.nextInt(6);
        int currentFood = foodCount.get();
        int newTotal = Math.min(currentFood + newFood, 100);
        foodCount.set(newTotal);
    }
    
    public void performSleep() {
    }
    
    public void performPlay() {
    }
    
    public void performClean() {
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
    
    public int getFoodCount() {
        return foodCount.get();
    }
    
    public IntegerProperty getFoodCountProperty() {
        return foodCount;
    }
    
    @Override
    public void onTick(double timeDelta) {
    }
    
    @Override
    public void onWeatherChanged(WeatherState newWeather) {
    }
}
