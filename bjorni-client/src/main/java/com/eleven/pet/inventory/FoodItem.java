package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.core.AssetLoader;

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
        // Let the Item handle the Pet's stats so the behavior is delegated to the Item
        // rather than the Model itself for better separation of concerns
        // This also allows for easier addition of new Item types in the future
        if (pet.getStats().hasStat(PetStats.STAT_HUNGER)) {
            pet.getStats().modifyStat(PetStats.STAT_HUNGER, statsRestore);
            return true;
        }
        return false;
    }

    @Override
    public String getSoundName() {
        return "eat";
    }
}
