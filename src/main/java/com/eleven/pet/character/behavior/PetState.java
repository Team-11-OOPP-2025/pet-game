package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.MinigameResult;

/**
 * Defines the contract for a behavioral state of a {@link PetModel}.
 *
 * <p>Implementations encapsulate how the pet reacts to user actions
 * (consume, play, sleep, clean) and how it is updated over time while
 * the state is active.</p>
 */
public interface PetState {

    /**
     * Handle a request for the pet to consume the given {@link Item}.
     *
     * @param pet  the pet whose state should handle the consumption
     * @param item the item to consume
     * @return {@code true} if the item was successfully consumed and applied,
     *         {@code false} otherwise
     */
    boolean handleConsume(PetModel pet, Item item);

    /**
     * Handle a request for the pet to play a minigame.
     *
     * @param pet the pet that should play
     * @return the {@link MinigameResult} if a minigame was started,
     *         or {@code null} if the action could not be performed
     */
    MinigameResult handlePlay(PetModel pet);

    /**
     * Handle a request for the pet to sleep or otherwise transition into
     * a sleeping-related state.
     *
     * @param pet the pet that should go to sleep
     */
    void handleSleep(PetModel pet);

    /**
     * Handle a request to clean the pet.
     *
     * @param pet the pet being cleaned
     */
    void handleClean(PetModel pet);

    /**
     * Periodic update hook called while this state is active.
     *
     * @param pet       the pet to update
     * @param timeDelta the elapsed time since the last tick, in game time units
     */
    void onTick(PetModel pet, double timeDelta);

    /**
     * Returns the canonical, unique name of this state.
     *
     * @return the identifier for this state
     */
    String getStateName();
}


