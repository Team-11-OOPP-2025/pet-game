package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;

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
     * {@code false} otherwise
     */
    boolean handleConsume(PetModel pet, Item item);

    /**
     * Checks if the pet is currently able to play a minigame.
     *
     * @param pet the pet to check
     * @return {@code true} if the pet can play, {@code false} if they are busy/asleep
     */
    boolean canPlay(PetModel pet);

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
     * @return {@code true} if the pet was successfully cleaned, {@code false} otherwise
     */
    boolean handleClean(PetModel pet);

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

    /**
     * Returns the time scale multiplier active during this state.
     * Default is normal speed (1.0).
     */
    default double getTimeScale() {
        return 1.0;
    }

    /**
     * Determines if the pet is currently awake and eligible to sleep.
     */
    default boolean canSleep() {
        return true; 
    }

    /**
     * Returns the sound name to be played when the pet enters this state.
     *
     * @return sound name, or {@code null} for no sound
     */
    default String getSoundName() {
        return null;
    }
}