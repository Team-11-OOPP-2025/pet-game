package com.eleven.pet.character;

import com.eleven.pet.core.GameConfig;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages sprites stats such as hunger, happiness, energy, and cleanliness.
 */
public class PetStats {
    public static final String STAT_HUNGER = "HUNGER";
    public static final String STAT_HAPPINESS = "HAPPINESS";
    public static final String STAT_ENERGY = "ENERGY";
    public static final String STAT_CLEANLINESS = "CLEANLINESS";

    private final Map<String, IntegerProperty> stats = new HashMap<>();

    /**
     * Register a new stat with an initial value.
     *
     * @param name         Name of the stat
     * @param initialValue Initial value of the stat
     */
    public void registerStat(String name, int initialValue) {
        int validValue = validate(initialValue);
        stats.put(name, new SimpleIntegerProperty(validValue));
    }

    /**
     * Get the IntegerProperty for a given stat.
     *
     * @param name Name of the stat
     * @return IntegerProperty of the stat, or null if not found
     */
    public IntegerProperty getStat(String name) {
        return stats.get(name);
    }

    /**
     * Get a copy of all stats.
     *
     * @return Map of all stats
     */
    public Map<String, IntegerProperty> getAllStats() {
        return new HashMap<>(stats);
    }

    /**
     * Modify a stat by a delta value.
     *
     * @param name  Name of the stat
     * @param delta Amount to modify the stat by (can be negative)
     * @return true if the stat was modified, false if the stat does not exist
     */
    public boolean modifyStat(String name, int delta) {
        IntegerProperty stat = getStat(name);
        if (stat != null) {
            int newVal = validate(stat.get() + delta);
            stat.set(newVal);
            return true;
        }
        return false;
    }

    /**
     * Calculate derived happiness based on hunger, energy, and cleanliness.
     * Happiness is the average of these three stats.
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
     * @param value Value to validate
     * @return Validated value within min and max bounds
     */
    private int validate(int value) {
        return Math.max(GameConfig.MIN_STAT_VALUE, Math.min(GameConfig.MAX_STAT_VALUE, value));
    }
}
