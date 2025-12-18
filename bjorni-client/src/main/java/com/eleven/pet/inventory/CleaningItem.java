package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;

public record CleaningItem(int id, String name, String imageFileName, int statsRestore) implements Item {


    /**
     * {@inheritDoc}
     */
    @Override
    public String description() {
        return "A cleaning item that restores " + statsRestore + " cleanliness.";
    }

    /**
     * Restores cleanliness on the given pet by {@link #statsRestore()}.
     *
     * @param pet target pet model
     * @return {@code true} if the stat was successfully modified
     */
    @Override
    public boolean use(PetModel pet) {
        // Let the Item handle the Pet's stats so the behavior is delegated to the Item
        // rather than the Model itself for better separation of concerns
        // This also allows for easier addition of new Item types in the future
        if (pet.getStats().hasStat(PetStats.STAT_CLEANLINESS)) {
            pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, statsRestore);
            return true;
        }
        return false;
    }
    
}
