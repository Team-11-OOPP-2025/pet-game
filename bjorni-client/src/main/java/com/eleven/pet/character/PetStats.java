package com.eleven.pet.character;

import com.eleven.pet.core.GameConfig;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages pet stats such as hunger, happiness, energy, and cleanliness.
 *
 * <p>All stats are stored as {@link IntegerProperty} to support UI bindings.
 * Values are automatically clamped between
 * {@link GameConfig#MIN_STAT_VALUE} and {@link GameConfig#MAX_STAT_VALUE}.</p>
 */
public class PetStats {
    /** Stat key for hunger (higher is better). */
    public static final String STAT_HUNGER = "HUNGER";
    /** Stat key for happiness. */
    public static final String STAT_HAPPINESS = "HAPPINESS";
    /** Stat key for energy. */
    public static final String STAT_ENERGY = "ENERGY";
    /** Stat key for cleanliness. */
    public static final String STAT_CLEANLINESS = "CLEANLINESS";

    private final Map<String, IntegerProperty> stats = new HashMap<>();

    /**
     * Register a new stat with an initial value.
     *
     * <p>If the stat already exists, its value is overwritten.</p>
     *
     * @param name         name of the stat
     * @param initialValue initial value of the stat, clamped to valid range
     */
    public void registerStat(String name, int initialValue) {
        int validValue = validate(initialValue);
        stats.put(name, new SimpleIntegerProperty(validValue));
    }

    /**
     * Get the {@link IntegerProperty} for a given stat.
     *
     * @param name name of the stat
     * @return {@code IntegerProperty} of the stat, or {@code null} if not found
     */
    public IntegerProperty getStat(String name) {
        return stats.get(name);
    }

    /**
     * Get a copy of all stats.
     *
     * <p>The returned map is a shallow copy; each value is the same
     * {@link IntegerProperty} instance used internally.</p>
     *
     * @return map of all stat names to their properties
     */
    public Map<String, IntegerProperty> getAllStats() {
        return new HashMap<>(stats);
    }

    /**
     * Modify a stat by a delta value.
     *
     * <p>The new value is clamped between
     * {@link GameConfig#MIN_STAT_VALUE} and {@link GameConfig#MAX_STAT_VALUE}.</p>
     *
     * @param name  name of the stat
     * @param delta amount to modify the stat by (can be negative)
     */
    public void modifyStat(String name, int delta) {
        IntegerProperty stat = getStat(name);
        if (stat != null) {
            int newVal = validate(stat.get() + delta);
            stat.set(newVal);
        }
    }

    /**
     * Check if a stat exists.
     *
     * @param name name of the stat
     * @return {@code true} if the stat exists, {@code false} otherwise
     */
    public boolean hasStat(String name) {
        return stats.containsKey(name);
    }

    /**
     * Calculate derived happiness based on hunger, energy, and cleanliness.
     *
     * <p>Happiness is set to the rounded average of these three stats.
     * If any required stat is missing, the method returns without changes.</p>
     */
    public void calculateDerivedHappiness() {
        IntegerProperty hunger = getStat(STAT_HUNGER);
        IntegerProperty energy = getStat(STAT_ENERGY);
        IntegerProperty cleanliness = getStat(STAT_CLEANLINESS);
        IntegerProperty happiness = getStat(STAT_HAPPINESS);

        // If any required stat is missing, do nothing
        if (hunger == null || energy == null || cleanliness == null || happiness == null) {
            return;
        }

        // All bars: higher = better
        int sum = hunger.get() + energy.get() + cleanliness.get();
        int average = Math.round(sum / 3.0f);

        happiness.set(validate(average));
    }

    /**
     * Validate that a stat value is within allowed bounds.
     *
     * @param value value to validate
     * @return validated value within min and max bounds
     */
    private int validate(int value) {
        return Math.max(GameConfig.MIN_STAT_VALUE, Math.min(GameConfig.MAX_STAT_VALUE, value));
    }
}
