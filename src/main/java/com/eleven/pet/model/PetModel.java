package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
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

    public WeatherSystem getWeatherSystem() {
        return weatherSystem;
    }

    public GameClock getClock() {
        return clock;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public PetStats getStats() {
        return stats;
    }

    public long getSleepStartTime() {
        return sleepStartTime;
    }

    public void setSleepStartTime(long sleepStartTime) {
        this.sleepStartTime = sleepStartTime;
    }

    public boolean hasSleptThisNight() {
        return sleptThisNight;
    }

    public void setSleptThisNight(boolean sleptThisNight) {
        this.sleptThisNight = sleptThisNight;
    }

    public GameClock getGameClock() {
        return null;
    }

    public FlightRecorder getFoodCountProperty() {
        return null;
    }

    public char[] getFoodCount() {
        return null;
    }

    public void clean() {
    }

    public void feed() {
    }

    public void play() {
    }

    public void sleep() {
    }

    public void replenishDailyFood() {
    }
}
