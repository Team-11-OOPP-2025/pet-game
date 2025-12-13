package com.eleven.pet.core;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Global game configuration constants.
 *
 * <p>All fields are public static final and represent immutable configuration values
 * used throughout the game (timings, rates, thresholds, penalties, and file settings).
 */
public final class GameConfig {
    /**
     * Window width in pixels.
     */
    public static final int WINDOW_WIDTH = 1920;

    /**
     * Window height in pixels.
     */
    public static final int WINDOW_HEIGHT = 1080;

    /**
     * Application title shown in the window chrome.
     */
    public static final String APP_TITLE = "Björni";

    /**
     * Hunger decrease per in-game second.
     */
    public static final double HUNGER_DECAY_RATE = 0.5;

    /**
     * Energy decrease per in-game second.
     */
    public static final double HAPPINESS_DECAY_RATE = 0.3;

    /**
     * Cleanliness decrease per in-game second.
     */
    public static final double CLEANLINESS_DECAY_RATE = 0.2;

    /**
     * Energy restored per in-game hour of sleep.
     *
     * <p>Example: If recovering 5 energy per hour, a 5-hour sleep (approx 5 sec real time at 2x)
     * restores ~25 energy.
     */
    public static final int SLEEP_ENERGY_PER_HOUR = 5;

    /**
     * Happiness restored per in-game hour of sleep.
     */
    public static final int SLEEP_HAPPINESS_PER_HOUR = 2;

    /**
     * Duration of one in-game day in real seconds (without acceleration).
     */
    public static final double DAY_LENGTH_SECONDS = 24.0;

    /**
     * Normal timescale multiplier.
     */
    public static final double TIMESCALE_NORMAL = 1.0;

    /**
     * Timescale multiplier used while sleeping (faster passage).
     */
    public static final double TIMESCALE_SLEEP = 2.0;

    /**
     * Hour of the day when the sprites typically wakes up (0.0 - 24.0).
     */
    public static final double HOUR_WAKE_UP = 8.0;           // 8:00 AM

    /**
     * Start hour (inclusive) of the preferred sleep window (0.0 - 24.0).
     */
    public static final double HOUR_SLEEP_WINDOW_START = 20.0; // 8:00 PM

    /**
     * End hour (exclusive) of the preferred sleep window (0.0 - 24.0).
     */
    public static final double HOUR_SLEEP_WINDOW_END = 2.0;    // 2:00 AM

    /**
     * Energy penalty applied when the sprites misses required sleep.
     */
    public static final int MISSED_SLEEP_ENERGY_PENALTY = 30;

    /**
     * Happiness penalty applied when the sprites misses required sleep.
     */
    public static final int MISSED_SLEEP_HAPPINESS_PENALTY = 20;

    /**
     * Energy threshold below which the sprites is considered "tired".
     */
    public static final int SLEEP_ENERGY_THRESHOLD = 20;

    /**
     * Minimum allowed stat value.
     */
    public static final int MIN_STAT_VALUE = 0;

    /**
     * Maximum allowed stat value.
     */
    public static final int MAX_STAT_VALUE = 100;

    /**
     * Interval between automatic saves in real seconds.
     */
    public static final double AUTOSAVE_INTERVAL_SECONDS = 30.0;

    /**
     * Timeout in seconds to wait for the save executor to shut down.
     */
    public static final int SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;

    /**
     * Path to the save file.
     */
    public static final Path SAVE_PATH = Paths.get("savegame.dat");

    /**
     * Current save file version string.
     */
    public static final String SAVE_FILE_VERSION = "0.0.2"; // an increment, I am getting tears! I never thought we'd see this day

    /**
     * Interval (in in-game seconds) between weather changes.
     */
    public static final int WEATHER_CHANGE_INTERVAL = 30;
}