package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;

/**
 * Common contract for all minigames that can be played with a pet.
 */
public interface Minigame {

    /**
     * Returns the display name of the minigame.
     *
     * @return the human-readable name of the game
     */
    String getName();

    /**
     * Executes or runs the minigame for the given pet, producing a result.
     *
     * @param pet the pet whose state may be modified by the game
     * @return a {@link MinigameResult} describing the outcome
     */
    MinigameResult play(PetModel pet);
}
