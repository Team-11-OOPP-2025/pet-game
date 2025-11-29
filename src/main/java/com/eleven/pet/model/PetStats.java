package com.eleven.pet.model;

import java.util.HashMap;
import java.util.Map;

import com.eleven.pet.config.GameConfig;

import javafx.beans.property.IntegerProperty;

public class PetStats {
    public static final String STAT_HUNGER = "hunger";
    public static final String STAT_HAPPINESS = "happiness";
    public static final String STAT_ENERGY = "energy";
    public static final String STAT_CLEANLINESS = "cleanliness";

    private final Map<String, IntegerProperty> stats = new HashMap<>();
    private final int MIN_VALUE = GameConfig.MIN_STAT_VALUE;
    private final int MAX_VALUE = GameConfig.MAX_STAT_VALUE;

    public void registerStat(String name, int initialValue) {
        // TODO: Implement stat registration with validation
    }

    public IntegerProperty getStat(String name) {
        // TODO: Implement get stat property
        return null;
    }

    public Map<String, IntegerProperty> getAllStats() {
        // TODO: Implement get all stats
        return null;
    }

    public void modifyStat(String name, int delta) {
        // TODO: Implement modify stat with validation
    }
    
    public void calculateDerivedHappiness() {
        // TODO: Implement derived happiness calculation
    }
    
    private int validate(int value) {
        // TODO: Implement validation (clamp between MIN and MAX)
        return 0;
    }
}
