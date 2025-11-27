package com.eleven.pet.config;

public final class GameConfig {
    private GameConfig() {}

    // Stat decay rates (per second)
    public static final double HUNGER_DECAY_RATE = 0.5;
    public static final double ENERGY_DECAY_RATE = 0.3;
    public static final double CLEANLINESS_DECAY_RATE = 0.2;

    // Recovery rates
    public static final double SLEEP_RECOVERY_RATE = 1.0;

    // Time system
    public static final double DAY_LENGTH_SECONDS = 24.0; // Your 24-second cycle!
    public static final double NIGHT_START_TIME = 0.5;

    // Action effects
    public static final int FEED_HUNGER_RESTORE = 30;
    public static final int PLAY_HAPPINESS_BOOST = 20;
    public static final int CLEAN_CLEANLINESS_RESTORE = 40;

    // State thresholds
    public static final int SLEEP_ENERGY_THRESHOLD = 20;
    public static final int WAKE_ENERGY_THRESHOLD = 80;
    public static final long SLEEP_DURATION_MS = 5000; // 5 seconds minimum sleep

    // Stat bounds
    public static final int MIN_STAT_VALUE = 0;
    public static final int MAX_STAT_VALUE = 100;
    
    // Autosave
    public static final double AUTOSAVE_INTERVAL_SECONDS = 60.0;
    
    // Save file
    public static final int SAVE_FILE_VERSION = 1;
    
    // Weather
    public static final int WEATHER_CHANGE_INTERVAL = 30; // seconds
}
