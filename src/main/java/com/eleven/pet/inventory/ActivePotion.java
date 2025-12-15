package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;

/**
 * Runtime representation of an active stat-modifying potion effect.
 *
 * <p>Wraps a {@link StatPotionDefinition} and tracks remaining duration
 * in game hours. Instances are managed by {@link PetModel}.</p>
 */
public class ActivePotion {
    private final StatPotionDefinition def;
    private double timeRemaining;

    /**
     * Creates a new active potion with full duration.
     *
     * @param def underlying potion definition
     */
    public ActivePotion(StatPotionDefinition def) {
        this.def = def;
        this.timeRemaining = def.effectDuration();
    }

    /**
     * Advances the internal timer by the given delta.
     *
     * @param delta elapsed time in game hours since the last tick
     */
    public void tick(double delta) {
        timeRemaining -= delta;
    }

    /**
     * Indicates whether this effect has fully expired.
     *
     * @return {@code true} if no time is remaining, {@code false} otherwise
     */
    public boolean isExpired() {
        return timeRemaining <= 0;
    }

    /**
     * Returns the remaining effect duration.
     *
     * @return remaining time in game hours
     */
    public double getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Returns the stat type affected by this potion.
     *
     * @return stat key (e.g. {@link PetStats#STAT_HAPPINESS})
     */
    public String getStatType() {
        return def.statType();
    }

    /**
     * Returns the multiplier applied to the stat while this potion is active.
     *
     * @return multiplicative factor
     */
    public double getMultiplier() {
        return def.multiplier();
    }

    /**
     * Returns the display name of the potion effect.
     *
     * @return human-readable name
     */
    public String getName() {
        return def.name();
    }
}