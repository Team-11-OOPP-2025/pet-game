package com.eleven.pet.environment.time;

import java.util.ArrayList;
import java.util.List;

import com.eleven.pet.config.GameConfig;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GameClock {
    private final List<TimeListener> listeners;
    private final DoubleProperty gameTime;
    private final ObjectBinding<DayCycle> currentCycle;
    private final double TIME_SCALE = 1.0;
    private final double NIGHT_THRESHOLD;
    private boolean paused;

    public GameClock() {
        this.listeners = new ArrayList<>();
        this.gameTime = new SimpleDoubleProperty(0.0);
        this.NIGHT_THRESHOLD = GameConfig.NIGHT_START_TIME;
        this.paused = false;
        
        this.currentCycle = Bindings.createObjectBinding(
            this::calculateCycle,
            gameTime
        );
    }
    
    public void subscribe(TimeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public boolean tick(double realTimeElapsed) {
        if (paused) {
            return false;
        }
        
        double scaledTime = realTimeElapsed * TIME_SCALE;
        gameTime.set(gameTime.get() + scaledTime);
        
        // Notify all listeners
        for (TimeListener listener : listeners) {
            listener.onTick(scaledTime);
        }
        
        boolean newDayStarted = false;
        
        if (gameTime.get() >= GameConfig.DAY_LENGTH_SECONDS) {
            startNewDay();
            newDayStarted = true;
        }
        
        return newDayStarted;
    }

    public void startNewDay() {
        gameTime.set(gameTime.get() % GameConfig.DAY_LENGTH_SECONDS);
        System.out.println("🔄 New day cycle started!");
    }
    
    public void setPaused(boolean paused) {
        this.paused = paused;
        System.out.println(paused ? "⏸️ Game paused" : "▶️ Game resumed");
    }
    
    public boolean isPaused() {
        return paused;
    }
    
    private DayCycle calculateCycle() {
        return gameTime.get() < NIGHT_THRESHOLD ? DayCycle.DAY : DayCycle.NIGHT;
    }
    
    public DayCycle getCycle() {
        return currentCycle.get();
    }
    
    public ObjectBinding<DayCycle> cycleProperty() {
        return currentCycle;
    }
}
