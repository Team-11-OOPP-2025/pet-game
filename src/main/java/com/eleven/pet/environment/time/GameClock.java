package com.eleven.pet.environment.time;

import com.eleven.pet.core.GameConfig;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GameClock {
    private final List<TimeListener> listeners = new ArrayList<>();
    private final DoubleProperty gameTime = new SimpleDoubleProperty(0.0);
    private final ObjectBinding<DayCycle> currentCycle;
    private double TIME_SCALE = GameConfig.TIMESCALE_NORMAL;
    private boolean paused = false;

    public GameClock() {
        // Start at 12:00 (noon) - 12/24 = 0.5 of the day
        double noonTime = (12.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        gameTime.set(noonTime);

        currentCycle = Bindings.createObjectBinding(
                this::calculateCycle,
                gameTime
        );
    }

    public void subscribe(TimeListener listener) {
        listeners.add(listener);
    }

    public boolean tick(double realTimeElapsed) {
        if (paused) return false;

        double previousTime = gameTime.get();
        double scaledDelta = realTimeElapsed * TIME_SCALE;
        gameTime.set((previousTime + scaledDelta) % GameConfig.DAY_LENGTH_SECONDS);

        for (TimeListener listener : listeners) {
            listener.onTick(scaledDelta);
        }

        return previousTime > gameTime.get(); // true: New day started
    }

    public void setTimeScale(double scale) {
        this.TIME_SCALE = Math.max(0.1, Math.min(scale, 10.0)); // Clamp between 0.1x and 10x
    }

    public double getTimeScale() {
        return TIME_SCALE;
    }


    private DayCycle calculateCycle() {
        // === STEP 1: NORMALIZE TIME ===
        // Convert game time to fraction of full day (0.0 = midnight, 1.0 = next midnight)
        double normalizedTime = gameTime.get() / GameConfig.DAY_LENGTH_SECONDS;

        // === STEP 2: MAP TIME TO CYCLE ===
        // Check thresholds from latest to earliest to handle wrap-around

        // DEEP_NIGHT: 00:00 (91.7%) to 04:00 (20.8%)
        if (normalizedTime >= 0.0 && normalizedTime < 0.1667) {
            return DayCycle.DEEP_NIGHT;
        }

        // DAWN: 04:00 (16.67%) to 07:00 (29.17%)
        else if (normalizedTime >= 0.1667 && normalizedTime < 0.2917) {
            return DayCycle.DAWN;
        }

        // MORNING: 07:00 (29.17%) to 11:00 (45.83%)
        else if (normalizedTime >= 0.2917 && normalizedTime < 0.4583) {
            return DayCycle.MORNING;
        }

        // DAY: 11:00 (45.83%) to 17:00 (70.83%)
        else if (normalizedTime >= 0.4583 && normalizedTime < 0.7083) {
            return DayCycle.DAY;
        }

        // EVENING: 17:00 (70.83%) to 21:00 (87.5%)
        else if (normalizedTime >= 0.7083 && normalizedTime < 0.875) {
            return DayCycle.EVENING;
        }

        // EARLY_NIGHT: 21:00 (87.5%) to 00:00 (100%)
        else {
            return DayCycle.EARLY_NIGHT;
        }
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
