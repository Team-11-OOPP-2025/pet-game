package com.eleven.pet.time;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class GameClock {
    private double currentGameTime;
    private final double gameDayLength; 
    private final SimpleBooleanProperty isDaytime;
    

    public GameClock() {
        this.gameDayLength = 24.0; // 24 seconds for a full day/night cycle
        this.currentGameTime = 0.0;
        this.isDaytime = new SimpleBooleanProperty(true); // Start with daytime
    }
    

    public boolean tick(double realTimeElapsed) {
        currentGameTime += realTimeElapsed;
        
        boolean newDayStarted = false;
        
        if (currentGameTime >= gameDayLength) {
            triggerNextDay();
            newDayStarted = true;
        }
        
        boolean shouldBeDaytime = currentGameTime < (gameDayLength / 2.0);
        if (isDaytime.get() != shouldBeDaytime) {
            isDaytime.set(shouldBeDaytime);
            System.out.println(shouldBeDaytime ? "☀️ Daytime begins!" : "🌙 Nighttime begins!");
        }
        
        return newDayStarted;
    }
    

    public void triggerNextDay() {
        currentGameTime = currentGameTime % gameDayLength; // Wrap around
        System.out.println("🔄 New day cycle started!");
    }
    

    public boolean isDaytime() {
        return isDaytime.get();
    }
    

    public ReadOnlyBooleanProperty isDaytimeProperty() {
        return isDaytime;
    }
    

    public double getCurrentGameTime() {
        return currentGameTime;
    }
    

    public double getGameDayLength() {
        return gameDayLength;
    }
}
