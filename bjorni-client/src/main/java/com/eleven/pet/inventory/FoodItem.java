package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

/**
 * Simple consumable item that restores a fixed amount of hunger.
 *
 * @param id             unique item identifier
 * @param name           display name of the food item
 * @param imageFileName  image used to render this item
 * @param statsRestore   amount of hunger restored when consumed
 */
public record FoodItem(int id, String name, String imageFileName, int statsRestore) implements Item {

    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "A tasty treat that restores " + statsRestore + " hunger.";
    }

    /**
     * Restores hunger on the given pet by {@link #statsRestore()}.
     *
     * @param pet target pet model
     * @return {@code true} if the stat was successfully modified
     */
    @Override
    public boolean use(PetModel pet) {
        return pet.eat(statsRestore);
    }

    @Override
    public String getSoundName() {
        return "eat";
    }
}
