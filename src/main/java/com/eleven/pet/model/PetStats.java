package com.eleven.pet.model;

import com.eleven.pet.config.GameConfig;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

public class PetStats {
    public static final String STAT_HUNGER = "HUNGER";
    public static final String STAT_HAPPINESS = "HAPPINESS";
    public static final String STAT_ENERGY = "ENERGY";
    public static final String STAT_CLEANLINESS = "CLEANLINESS";

    private final Map<String, IntegerProperty> stats = new HashMap<>();

    public void registerStat(String name, int initialValue) {
        int validValue = validate(initialValue);
        stats.put(name, new SimpleIntegerProperty(validValue));
    }

    public IntegerProperty getStat(String name) {
        return stats.get(name);
    }

    public Map<String, IntegerProperty> getAllStats() {
        return new HashMap<>(stats);
    }

    public boolean modifyStat(String name, int delta) {
        IntegerProperty stat = getStat(name);
        if (stat != null) {
            int newVal = validate(stat.get() + delta);
            stat.set(newVal);
            return true;
        }
        return false;
    }

    public void calculateDerivedHappiness() {
        IntegerProperty hunger = getStat(STAT_HUNGER);
        IntegerProperty energy = getStat(STAT_ENERGY);
        IntegerProperty cleanliness = getStat(STAT_CLEANLINESS);
        IntegerProperty happiness = getStat(STAT_HAPPINESS);

        if (hunger == null || energy == null || cleanliness == null || happiness == null) {
            return;
        }

        // Lower hunger is better, so invert it
        int hungerScore = 100 - hunger.get();
        int energyScore = energy.get();
        int cleanlinessScore = cleanliness.get();

        // Weighted average (adjust weights to match assignment spec)
        int derived = (int) Math.round(
                0.4 * hungerScore +
                        0.3 * energyScore +
                        0.3 * cleanlinessScore
        );

        happiness.set(validate(derived));
    }


    private int validate(int value) {
        return Math.max(GameConfig.MIN_STAT_VALUE, Math.min(GameConfig.MAX_STAT_VALUE, value));
    }
}
