package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

/**
 * Item implementation representing a stat-boosting potion.
 * <p>
 * When used, the potion applies the effect described by its
 * {@link StatPotionDefinition} to the given {@link PetModel}.
 */
public class StatPotion implements Item {
    private final int id;
    private final StatPotionDefinition definition;

    /**
     * Creates a new {@code StatPotion} instance.
     *
     * @param id          unique item identifier
     * @param definition  stat effect definition for this potion
     */
    public StatPotion(int id, StatPotionDefinition definition) {
        this.id = id;
        this.definition = definition;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int id() {
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String name() {
        return definition.name();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "A potion that boosts " + definition.statType() + " by " + definition.multiplier() + "x for " + definition.effectDuration() + " seconds.";
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stat potions do not directly restore stats; they apply a temporary effect instead.
     */
    @Override
    public int statsRestore() {
        return 0;
    }

    /**
     * Applies this potion's effect to the given pet.
     *
     * @param pet target pet model
     * @return {@code true} if the effect was applied
     */
    @Override
    public boolean use(PetModel pet) {
        pet.addPotion(definition);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String imageFileName() {
        return "energypotion";
    }
}