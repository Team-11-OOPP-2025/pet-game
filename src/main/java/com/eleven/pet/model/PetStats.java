package com.eleven.pet.model;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PetStats {
    public static final String STAT_HUNGER = "hunger";
    public static final String STAT_HAPPINESS = "happiness";
    public static final String STAT_ENERGY = "energy";
    public static final String STAT_CLEANLINESS = "cleanliness";
    
    private final Map<String, IntegerProperty> stats;
    private final int MIN_VALUE;
    private final int MAX_VALUE;
    
    public PetStats(int minValue, int maxValue) {
        this.MIN_VALUE = minValue;
        this.MAX_VALUE = maxValue;
        this.stats = new HashMap<>();
        
        // Initialize all stats
        registerStat(STAT_HUNGER, 50);
        registerStat(STAT_HAPPINESS, 50);
        registerStat(STAT_ENERGY, 50);
        registerStat(STAT_CLEANLINESS, 50);
    }
    
    public void registerStat(String name, int val) {
        stats.put(name, new SimpleIntegerProperty(validate(val)));
    }
    
    public IntegerProperty getStat(String name) {
        return stats.get(name);
    }
    
    public Map<String, IntegerProperty> getAllStats() {
        return stats;
    }
    
    public void modifyStat(String name, int delta) {
        IntegerProperty stat = stats.get(name);
        if (stat != null) {
            stat.set(validate(stat.get() + delta));
        }
    }
    
    public void calculateDerivedHappiness() {
        // Happiness is influenced by other stats
        int hunger = getStat(STAT_HUNGER).get();
        int energy = getStat(STAT_ENERGY).get();
        int clean = getStat(STAT_CLEANLINESS).get();
        
        // Average of other stats with some weighting
        int derived = (hunger + energy + clean) / 3;
        
        // Blend current happiness with derived value (gradual adjustment)
        int current = getStat(STAT_HAPPINESS).get();
        int blended = (int)(current * 0.8 + derived * 0.2);
        
        getStat(STAT_HAPPINESS).set(validate(blended));
    }
    
    private int validate(int val) {
        return Math.max(MIN_VALUE, Math.min(MAX_VALUE, val));
    }
}
