package com.eleven.pet.environment.clock;

import java.util.ArrayList;
import java.util.List;

import com.eleven.pet.config.GameConfig;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GameClock {
    private final List<TimeListener> listeners = new ArrayList<>();
    private final DoubleProperty gameTime = new SimpleDoubleProperty(0.0);
    private final ObjectBinding<DayCycle> currentCycle;
    private final double TIME_SCALE = 1.0;
    private final double NIGHT_THRESHOLD = GameConfig.NIGHT_START_TIME;
    private boolean paused = false;

    public GameClock() {
        currentCycle = Bindings.createObjectBinding(
                this::calculateCycle,
                gameTime
        );
    }

    public void subscribe(TimeListener listener) {
        listeners.add(listener);
    }

    public boolean tick(double realTimeElapsed)  {
        if (paused) return false;

        double previousTime = gameTime.get();
        double scaledDelta = realTimeElapsed * TIME_SCALE;
        gameTime.set((previousTime + scaledDelta) % GameConfig.DAY_LENGTH_SECONDS);

        for (TimeListener listener : listeners) {
            listener.onTick(scaledDelta);
        }

        return previousTime > gameTime.get(); // true: New day started
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private DayCycle calculateCycle() {
        double normalizedTime = gameTime.get() / GameConfig.DAY_LENGTH_SECONDS;
        return normalizedTime >= NIGHT_THRESHOLD ? DayCycle.NIGHT : DayCycle.DAY;
    }
    public DayCycle getCycle() {
        return currentCycle.get();
    }

    public ObjectBinding<DayCycle> cycleProperty() {
        return currentCycle;
    }
    
    public double getGameTime() {
        return gameTime.get();
    }
    
    public DoubleProperty gameTimeProperty() {
        return gameTime;
    }
}
