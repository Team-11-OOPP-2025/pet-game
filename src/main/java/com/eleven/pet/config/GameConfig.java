package com.eleven.pet.config;

public class GameConfig {
    // Stat decay rates (per second)
    public static final double HUNGER_DECAY_RATE = 1.0;
    public static final double ENERGY_DECAY_RATE = 0.8;
    public static final double SLEEP_RECOVERY_RATE = 5.0;
    public static final double CLEANLINESS_DECAY_RATE = 0.5;
    
    // Time settings
    public static final double DAY_LENGTH_SECONDS = 24.0;
    public static final double NIGHT_START_TIME = 12.0;
    public static final long SLEEP_DURATION_MS = 5000;
    
    // Persistence
    public static final double AUTOSAVE_INTERVAL_SECONDS = 60.0;
    public static final int SAVE_FILE_VERSION = 1;
    
    // Stat bounds
    public static final int MIN_STAT_VALUE = 0;
    public static final int MAX_STAT_VALUE = 100;
    
    // Action effects
    public static final int FEED_HUNGER_RESTORE = 20;
    public static final int PLAY_HAPPINESS_BOOST = 15;
    public static final int CLEAN_CLEANLINESS_RESTORE = 30;
    
    // Sleep thresholds
    public static final int SLEEP_ENERGY_THRESHOLD = 30;
    public static final int WAKE_ENERGY_THRESHOLD = 80;
    
    // Weather
    public static final int WEATHER_CHANGE_INTERVAL = 120;
}