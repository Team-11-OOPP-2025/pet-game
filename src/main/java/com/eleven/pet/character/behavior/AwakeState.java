package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.inventory.Item;
import com.google.auto.service.AutoService;

/**
 * Represents the awake behavior/state of a pet. Handles actions available while the pet is awake
 * such as consuming items, playing minigames, sleeping, cleaning, and time-based updates.
 *
 * <p>This class is registered via {@link AutoService} as an implementation of {@link PetState}.</p>
 */
@AutoService(PetState.class)
public class AwakeState implements PetState {
    public static final String STATE_NAME = "AWAKE";

    /**
     * Attempt to consume an {@link Item} from the pet's inventory and apply its effect.
     *
     * @param pet  the {@link PetModel} performing the consumption
     * @param item the {@link Item} to consume
     * @return {@code true} if the item was available and used successfully, {@code false} otherwise
     */
    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        if (pet.getInventory().canRemove(item, 1)) {  // QUERY 
            pet.getInventory().remove(item, 1);        //  COMMAND
            System.out.println(pet.getName() + " is consuming " + item.name() + ".");
            return item.use(pet);
        }
        System.out.println(item.name() + " is not available in inventory.");
        return false;
    }

    /**
     * Indicates that an awake pet is generally able to play minigames.
     *
     * @param pet the pet model
     * @return always {@code true}
     */
    @Override
    public boolean canPlay(PetModel pet) {
        return pet.getStats().getStat(PetStats.STAT_ENERGY).get() >= 10;
    }

    /**
     * Transition the pet into the sleeping state. Prepares sleep-related flags and resets sleep
     * tracking counters if a game clock is present.
     *
     * @param pet the {@link PetModel} that will go to sleep
     */
    @Override
    public void handleSleep(PetModel pet) {
        System.out.println(pet.getName() + " is getting ready to sleep...");

        // Initialize sleep flags BEFORE changing state
        pet.setSleptThisNight(true);
        pet.setPassedEightAM(false);

        if (pet.getClock() != null) {
            pet.setCurrentSleepDuration(0.0);
            pet.setHoursSleptRewardCount(0);
        }

        StateRegistry registry = StateRegistry.getInstance();
        PetState asleepState = registry.getState(AsleepState.STATE_NAME);
        if (asleepState != null) {
            pet.changeState(asleepState);
        }
    }

    /**
     * Clean the pet, providing a fixed cleanliness and small happiness benefit.
     * Requires at least 5 energy.
     *
     * @param pet the {@link PetModel} being cleaned
     */
    @Override
    public void handleClean(PetModel pet) {
        int currentEnergy = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        if (currentEnergy < 5) {
            System.out.println(pet.getName() + " is too tired to be cleaned right now.");
            return;
        }

        // Simple cleaning effect: improve cleanliness and a bit of happiness
        pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, 10);
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, 2);
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -5);
        System.out.println(pet.getName() + " has been cleaned.");
    }

    /**
     * Periodic update called by the game clock. Applies stat decay and manages daily sleep
     * checks and flags (missed sleep penalty at wake-up hour, resetting flags after the window).
     *
     * @param pet       the {@link PetModel} to update
     * @param timeDelta time elapsed since the last tick in game time units
     */
    @Override
    public void onTick(PetModel pet, double timeDelta) {
        if (pet.getClock() == null) return;

        double currentHour = pet.getCurrentGameHour();
        pet.applyStatDecay(timeDelta);

        // 1. Check for Missed Sleep Penalty at 8 AM
        if (currentHour >= GameConfig.HOUR_WAKE_UP && currentHour < (GameConfig.HOUR_WAKE_UP + 1.0)) {
            if (!pet.isPassedEightAM()) {
                if (!pet.isSleptThisNight()) {
                    applyMissedSleepPenalty(pet);
                }
                pet.setPassedEightAM(true);
            }
        }

        // 2. Reset the daily check flag after 9 AM
        if (currentHour >= (GameConfig.HOUR_WAKE_UP + 1.0) && pet.isPassedEightAM()) {
            pet.setPassedEightAM(false);
        }

        // 3. Reset sleep flag at 20:00 (Start of new sleep window)
        if (currentHour >= GameConfig.HOUR_SLEEP_WINDOW_START && pet.isSleptThisNight()) {
            pet.setSleptThisNight(false);
            System.out.println("It is now evening. Sleep flag reset.");
        }
    }

    /**
     * Apply penalties for missing sleep overnight. Adjusts energy and happiness stats.
     *
     * @param pet the {@link PetModel} to penalize
     */
    private void applyMissedSleepPenalty(PetModel pet) {
        pet.applyMissedSleepPenalty();
    }

    /**
     * Get the canonical name of this state.
     *
     * @return the state name identifier
     */
    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}