package com.eleven.pet.model;

import java.util.HashMap;
import java.util.Map;

import com.eleven.pet.config.GameConfig;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PetStats {
    public static final String STAT_HUNGER = "hunger";
    public static final String STAT_HAPPINESS = "happiness";
    public static final String STAT_ENERGY = "energy";
    public static final String STAT_CLEANLINESS = "cleanliness";

    private final Map<String, IntegerProperty> stats = new HashMap<>();
    private final int MIN_VALUE = GameConfig.MIN_STAT_VALUE;
    private final int MAX_VALUE = GameConfig.MAX_STAT_VALUE;

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

    public void modifyStat(String name, int delta) {
        IntegerProperty stat = getStat(name);
        if (stat != null) {
            int newVal = validate(stat.get() + delta);
            stat.set(newVal);
        }
    }
    
    public void calculateDerivedHappiness() {
        // TODO: Implement derived happiness calculation
    }
    
    private int validate(int value) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, value));
    }
}
