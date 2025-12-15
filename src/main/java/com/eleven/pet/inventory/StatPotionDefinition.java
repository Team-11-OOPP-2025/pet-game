package com.eleven.pet.inventory;

/**
 * Pure data class that defines the behavior of a stat potion.
 * <p>
 * A {@code StatPotionDefinition} describes which stat is affected,
 * for how long, and by what multiplier.
 */
public record StatPotionDefinition(
        /**
         * Display name of the potion.
         */
        String name,
        /**
         * Name of the affected stat (e.g. "ENERGY").
         */
        String statType,
        /**
         * Duration of the effect in seconds. Must be non-negative.
         */
        int effectDuration,
        /**
         * Multiplier applied to the affected stat while the potion is active.
         */
        double multiplier) {

    /**
     * Creates a new {@code StatPotionDefinition}.
     *
     * @param name           display name of the potion
     * @param statType       name of the affected stat
     * @param effectDuration duration of the effect in seconds (must be {@code >= 0})
     * @param multiplier     multiplier applied to the stat while active
     * @throws IllegalArgumentException if {@code effectDuration} is negative
     */
    public StatPotionDefinition {
        if (effectDuration < 0) {
            throw new IllegalArgumentException("Effect duration cannot be negative: " + effectDuration);
        }
    }
}