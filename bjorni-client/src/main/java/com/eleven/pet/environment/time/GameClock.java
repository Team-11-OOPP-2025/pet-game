package com.eleven.pet.environment.time;

import com.eleven.pet.core.GameConfig;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Central game time controller.
 * <p>
 * The {@code GameClock} tracks in‑game time in seconds, advances it based on
 * real elapsed time and a configurable time scale, and notifies registered
 * {@link TimeListener}s on each tick. It also exposes a derived
 * {@link DayCycle} based on the current in‑game time.
 * </p>
 */
@Data
public class GameClock {
    private final List<TimeListener> listeners = new ArrayList<>();
    private final DoubleProperty gameTime = new SimpleDoubleProperty(0.0);
    private final ObjectBinding<DayCycle> currentCycle;
    private double TIME_SCALE = GameConfig.TIMESCALE_NORMAL;
    private boolean paused = false;

    /**
     * Creates a new {@code GameClock} instance and initializes the in‑game time
     * to noon (12:00) of the current day.
     */
    public GameClock() {
        // Start at 12:00 (noon) - 12/24 = 0.5 of the day
        double noonTime = (12.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        gameTime.set(noonTime);

        currentCycle = Bindings.createObjectBinding(
                this::calculateCycle,
                gameTime
        );
    }

    /**
     * Registers a {@link TimeListener} to be notified on each tick.
     *
     * @param listener the listener to add; must not be {@code null}
     */
    public void subscribe(TimeListener listener) {
        listeners.add(listener);
    }

    /**
     * Advances the game clock by the specified real-time delta.
     * <p>
     * The provided {@code realTimeElapsed} is multiplied by the current time
     * scale, added to the internal game time and wrapped at
     * {@link GameConfig#DAY_LENGTH_SECONDS}. All registered listeners are
     * notified with the scaled delta.
     * </p>
     *
     * @param realTimeElapsed elapsed real time since the last tick, in seconds
     * @return {@code true} if the call caused the in‑game time to wrap around
     *         to the next day, {@code false} otherwise
     */
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

    /**
     * Sets the time scale used to convert real-time into game time.
     * <p>
     * The value is clamped to the range {@code [0.1, 10.0]}.
     * </p>
     *
     * @param scale the desired time scale factor
     */
    public void setTimeScale(double scale) {
        this.TIME_SCALE = Math.max(0.1, Math.min(scale, 10.0)); // Clamp between 0.1x and 10x
    }

    /**
     * Returns the current time scale factor.
     *
     * @return the time scale applied when advancing the clock
     */
    public double getTimeScale() {
        return TIME_SCALE;
    }

    /**
     * Computes the {@link DayCycle} value for the current {@link #gameTime}.
     *
     * @return the current day cycle segment
     */
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

    /**
     * Gets the current {@link DayCycle} for the game time.
     *
     * @return the active {@link DayCycle}
     */
    public DayCycle getCycle() {
        return currentCycle.get();
    }

    /**
     * Exposes a binding to the current {@link DayCycle} so UI code can
     * react to cycle changes.
     *
     * @return an {@link ObjectBinding} bound to the computed day cycle
     */
    public ObjectBinding<DayCycle> cycleProperty() {
        return currentCycle;
    }

    /**
     * Returns the current in‑game time in seconds since the start of the day.
     *
     * @return the game time in seconds
     */
    public double getGameTime() {
        return gameTime.get();
    }

    /**
     * Returns a JavaFX property representing the in‑game time.
     *
     * @return the {@link DoubleProperty} backing the game time
     */
    public DoubleProperty gameTimeProperty() {
        return gameTime;
    }
}
