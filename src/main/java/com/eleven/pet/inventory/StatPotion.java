package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

public class StatPotion implements Item {
    private final int id;
    private final StatPotionDefinition definition;


    public StatPotion(int id, StatPotionDefinition definition) {
        this.id = id;
        this.definition = definition;
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
        return 0;
    }

    @Override
    public boolean use(PetModel pet) {
        pet.addPotion(definition);
        return true;
    }

    @Override
    public String imageFileName() {
        return "energypotion";
    }


}