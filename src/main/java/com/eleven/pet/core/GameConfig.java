package com.eleven.pet.core;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class GameConfig {
    public static final int WINDOW_WIDTH = 1920;
    public static final int WINDOW_HEIGHT = 1080;
    public static final String APP_TITLE = "Björni";

    public static final double HUNGER_DECAY_RATE = 0.5;
    public static final double HAPPINESS_DECAY_RATE = 0.3;
    public static final double CLEANLINESS_DECAY_RATE = 0.2;

    public static final int SLEEP_ENERGY_PER_HOUR = 5;
    public static final int SLEEP_HAPPINESS_PER_HOUR = 2;

    public static final double DAY_LENGTH_SECONDS = 24.0;
    public static final double TIMESCALE_NORMAL = 1.0;
    public static final double TIMESCALE_SLEEP = 2.0;

    public static final double HOUR_WAKE_UP = 8.0;
    public static final double HOUR_SLEEP_WINDOW_START = 20.0; 
    public static final double HOUR_SLEEP_WINDOW_END = 2.0;

    public static final int MISSED_SLEEP_ENERGY_PENALTY = 30;
    public static final int MISSED_SLEEP_HAPPINESS_PENALTY = 20;
    public static final int SLEEP_ENERGY_THRESHOLD = 20;

    public static final int MIN_STAT_VALUE = 0;
    public static final int MAX_STAT_VALUE = 100;

    public static final double AUTOSAVE_INTERVAL_SECONDS = 30.0;
    public static final int SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;
    public static final Path SAVE_PATH = Paths.get("savegame.dat");

    public static final String SAVE_FILE_VERSION = "0.0.2"; 

    public static final int WEATHER_CHANGE_INTERVAL = 30;
    
    // Daily Reward Cooldown in Game Hours
    public static final double DAILY_REWARD_COOLDOWN = 24.0;
}