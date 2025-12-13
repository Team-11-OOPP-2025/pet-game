package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

public class StatPotion implements Item {
    private final int id;
    private final StatPotionDefinition definition;
    private final int statMultiplier;

    public StatPotion(int id, StatPotionDefinition definition, int statMultiplier) {
        this.id = id;
        this.definition = definition;
        this.statMultiplier = statMultiplier;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public String name() {
        return definition.name();
    }

    @Override
    public String description() {
        return "A potion that boosts " + definition.statType() + " by " + definition.multiplier() + "x for " + definition.effectDuration() + " seconds.";
    }

    @Override
    public int statsRestore() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'statsRestore'");
    }

    @Override
    public boolean use(PetModel pet) {
        pet.addPotion(definition);
        return true;
    }


}
