package com.eleven.pet.core;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Central configuration values for the game.
 *
 * <p>This class contains only constants and is not meant to be instantiated.
 * Values control window settings, gameplay tuning, persistence, and timing.
 */
public final class GameConfig {

    /** Default window width in pixels. */
    public static final int WINDOW_WIDTH = 1920;
    /** Default window height in pixels. */
    public static final int WINDOW_HEIGHT = 1080;
    /** Application window title. */
    public static final String APP_TITLE = "Björni";

    /** Rate at which hunger decreases per in‑game hour. */
    public static final double HUNGER_DECAY_RATE = 0.5;
    /** Rate at which happiness decreases per in‑game hour. */
    public static final double HAPPINESS_DECAY_RATE = 0.3;
    /** Rate at which cleanliness decreases per in‑game hour. */
    public static final double CLEANLINESS_DECAY_RATE = 0.2;

    /** Energy restored per in‑game hour of sleep. */
    public static final int SLEEP_ENERGY_PER_HOUR = 5;
    /** Happiness restored per in‑game hour of sleep. */
    public static final int SLEEP_HAPPINESS_PER_HOUR = 2;

    /** Length of a full in‑game day, in real‑time seconds. */
    public static final double DAY_LENGTH_SECONDS = 24.0;
    /** Default timescale multiplier for normal gameplay. */
    public static final double TIMESCALE_NORMAL = 1.0;
    /** Timescale multiplier applied while sleeping. */
    public static final double TIMESCALE_SLEEP = 2.0;

    /** Default in‑game wake‑up hour. */
    public static final double HOUR_WAKE_UP = 8.0;
    /** Start of the recommended sleep window (24h clock). */
    public static final double HOUR_SLEEP_WINDOW_START = 20.0;
    /** End of the recommended sleep window (24h clock, may wrap past midnight). */
    public static final double HOUR_SLEEP_WINDOW_END = 2.0;

    /** Energy penalty applied if sleep is missed. */
    public static final int MISSED_SLEEP_ENERGY_PENALTY = 30;
    /** Happiness penalty applied if sleep is missed. */
    public static final int MISSED_SLEEP_HAPPINESS_PENALTY = 20;
    /** Energy threshold below which sleep is recommended/required. */
    public static final int SLEEP_ENERGY_THRESHOLD = 20;

    /** Minimum allowed value for any pet stat. */
    public static final int MIN_STAT_VALUE = 0;
    /** Maximum allowed value for any pet stat. */
    public static final int MAX_STAT_VALUE = 100;

    /** Interval between autosaves, in seconds of real time. */
    public static final double AUTOSAVE_INTERVAL_SECONDS = 30.0;
    /** Timeout when shutting down the save executor, in seconds. */
    public static final int SAVE_EXECUTOR_SHUTDOWN_TIMEOUT_SECONDS = 5;
    /** Path of the main savegame file. */
    public static final Path SAVE_PATH = Paths.get("savegame.dat");

    /** Version tag stored in save files to detect incompatibilities. */
    public static final String SAVE_FILE_VERSION = "0.0.3";

    /** Interval between weather changes, in seconds of real time. */
    public static final int WEATHER_CHANGE_INTERVAL = 30;

    /** Cooldown between daily rewards, in in‑game hours. */
    public static final double DAILY_REWARD_COOLDOWN = 24.0;

}