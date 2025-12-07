package com.eleven.pet.core;

public final class GameConfig {
    // --- Stat decay rates (per game second) ---
    public static final double HUNGER_DECAY_RATE = 0.5;
    public static final double ENERGY_DECAY_RATE = 0.3;
    public static final double CLEANLINESS_DECAY_RATE = 0.2;

    // --- Recovery rates (per game second) ---
    // Example: If recovering 5 energy per second, a 5-hour sleep (approx 5 sec real time at 2x) restores ~50 energy
    public static final int SLEEP_ENERGY_PER_HOUR = 5;
    public static final int SLEEP_HAPPINESS_PER_HOUR = 2;

    // --- Time system ---
    // One in-game day lasts 24 real seconds (without acceleration)
    public static final double DAY_LENGTH_SECONDS = 24.0;

    // Time Scales
    public static final double TIME_SCALE_NORMAL = 1.0;
    public static final double TIME_SCALE_SLEEP = 2.0;

    // Day Cycle Hours (0.0 - 24.0)
    public static final double HOUR_WAKE_UP = 8.0;           // 8:00 AM
    public static final double HOUR_SLEEP_WINDOW_START = 20.0; // 8:00 PM
    public static final double HOUR_SLEEP_WINDOW_END = 2.0;    // 2:00 AM

    // --- Penalties ---
    public static final int MISSED_SLEEP_ENERGY_PENALTY = 30;
    public static final int MISSED_SLEEP_HAPPINESS_PENALTY = 20;

    // --- State thresholds ---
    public static final int SLEEP_ENERGY_THRESHOLD = 20; // Used to determine if pet is "tired"

    // --- Stat bounds ---
    public static final int MIN_STAT_VALUE = 0;
    public static final int MAX_STAT_VALUE = 100;

    // --- Autosave ---
    public static final double AUTOSAVE_INTERVAL_SECONDS = 30.0;
    public static final int SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;

    // --- Save file ---
    public static final String SAVE_FILE_VERSION = "0.0.1";

    // --- Weather ---
    public static final int WEATHER_CHANGE_INTERVAL = 30;
}