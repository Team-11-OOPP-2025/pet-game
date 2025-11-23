package com.eleven.pet.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.HashMap;
import java.util.Map;

public class PetStats {
    public static final String STAT_HUNGER = "hunger";
    public static final String STAT_HAPPINESS = "happiness";
    public static final String STAT_ENERGY = "energy";
    public static final String STAT_CLEANLINESS = "cleanliness";

    private final Map<String, IntegerProperty> stats = new HashMap<>();

    public void registerStat(String name, int initialValue) {
        stats.put(name, new SimpleIntegerProperty(initialValue));
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
            int newVal = Math.max(0, Math.min(100, stat.get() + delta));
            stat.set(newVal);
        }
    }
}
