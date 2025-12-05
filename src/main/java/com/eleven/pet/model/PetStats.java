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
        // TODO: Implement derived happiness calculation
    }

    private int validate(int value) {
        return Math.max(GameConfig.MIN_STAT_VALUE, Math.min(GameConfig.MAX_STAT_VALUE, value));
    }
}
