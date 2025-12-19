package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

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
        return pet.performClean();
    }

    @Override
    public String getSoundName() {
        return "clean";
    }
    
}
