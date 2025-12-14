package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;

public record FoodItem(int id, String name, String imageFileName, int statsRestore) implements Item {

    @Override
    public String description() {
        return "A tasty treat that restores " + statsRestore + " hunger.";
    }

    @Override
    public boolean use(PetModel pet) {
        // Let the Item handle the Pet's stats so the behavior is delegated to the Item
        // rather than the Model itself for better separation of concerns
        // This also allows for easier addition of new Item types in the future
        return pet.getStats().modifyStat(PetStats.STAT_HUNGER, statsRestore);
    }
}
