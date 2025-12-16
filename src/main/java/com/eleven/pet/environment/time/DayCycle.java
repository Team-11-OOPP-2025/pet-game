package com.eleven.pet.environment.time;

/**
 * Enumeration of the high-level parts of the in‑game day.
 * <p>
 * These values are derived from the current game time in {@link GameClock}
 * and can be used to drive lighting, ambiance, and behavior changes.
 * </p>
 */
public enum DayCycle {
    /** Early morning transition from night to day. */
    DAWN,
    /** Morning hours after dawn. */
    MORNING,
    /** Bright daytime period around midday and afternoon. */
    DAY,
    /** Late afternoon and sunset period. */
    EVENING,
    /** Early night after sunset. */
    EARLY_NIGHT,
    /** Deep night around midnight and early pre-dawn hours. */
    DEEP_NIGHT
}
