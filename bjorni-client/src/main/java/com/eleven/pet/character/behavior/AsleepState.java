package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.inventory.Item;
import com.google.auto.service.AutoService;

/**
 * Represents the sleeping behavior/state of a pet.
 *
 * <p>While asleep, most interactive actions (consume, play, clean) are
 * rejected or deferred. This state is responsible for tracking sleep
 * duration, rewarding stats during sleep, and automatically waking the
 * pet at the configured wake-up hour.</p>
 *
 * <p>This class is registered via {@link AutoService} as an implementation
 * of {@link PetState}.</p>
 */
@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "ASLEEP";

    /**
     * Rejects consumption attempts while the pet is asleep.
     *
     * @param pet  the sleeping pet
     * @param item the item the user attempted to feed
     * @return always {@code false}, as items cannot be consumed while asleep
     */
    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        System.out.println(pet.getName() + " is asleep and cannot eat right now.");
        return false;
    }

    /**
     * Indicates that the pet cannot play while asleep.
     *
     * @return always {@code false}
     */
    @Override
    public boolean canPlay(PetModel pet) {
        return false;
    }

    /**
     * Handles a manual wake-up triggered by the user while the pet is asleep.
     *
     * @param pet the pet to wake up
     */
    @Override
    public void handleSleep(PetModel pet) {
        // Manual Wake Up
        System.out.println("Player woke up " + pet.getName() + " manually.");
            
        wakeUp(pet);
    }

    /**
     * Rejects cleaning requests while the pet is asleep.
     *
     * @param pet the sleeping pet
     */
    @Override
    public boolean handleClean(PetModel pet) {
        System.out.println(pet.getName() + " is asleep and cannot be cleaned right now.");
        return false;
    }

    /**
     * Periodic update while the pet is asleep.
     *
     * <p>Accumulates sleep duration, periodically rewards energy and happiness
     * based on hours slept (taking stat multipliers into account), and
     * automatically wakes the pet at the configured wake-up hour.</p>
     *
     * @param pet       the sleeping pet
     * @param timeDelta elapsed time since last tick, in game time units
     */
    @Override
    public void onTick(PetModel pet, double timeDelta) {
        double newDuration = pet.getCurrentSleepDuration() + timeDelta;
        pet.setCurrentSleepDuration(newDuration);

        // Calculate total hours slept
        double secondsPerGameHour = GameConfig.DAY_LENGTH_SECONDS / 24.0;
        int totalHoursSlept = (int) (newDuration / secondsPerGameHour);

        // Reward energy and happiness for each hour slept
        int hoursToReward = totalHoursSlept - pet.getHoursSleptRewardCount();

        // Safety check: Prevent massive spikes if logic desyncs or saves are loaded weirdly
        if (hoursToReward > 10 && pet.getHoursSleptRewardCount() == 0) {
            pet.setHoursSleptRewardCount(totalHoursSlept);
            System.out.println("Synced sleep reward counter (prevented massive load spike).");
        }

        if (hoursToReward > 0) {
            // Get multipliers from active potions
            double energyMult = pet.getStatMultiplier(PetStats.STAT_ENERGY);
            double happyMult = pet.getStatMultiplier(PetStats.STAT_HAPPINESS);

            // Calculate base values
            int baseEnergy = hoursToReward * GameConfig.SLEEP_ENERGY_PER_HOUR;
            int baseHappy = hoursToReward * GameConfig.SLEEP_HAPPINESS_PER_HOUR;

            // Apply multipliers
            int finalEnergyGain = (int) (baseEnergy * energyMult);
            int finalHappyGain = (int) (baseHappy * happyMult);

            // Update Stats
            pet.getStats().modifyStat(PetStats.STAT_ENERGY, finalEnergyGain);
            pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, finalHappyGain);

            pet.setHoursSleptRewardCount(pet.getHoursSleptRewardCount() + hoursToReward);

            System.out.println("Sleep (" + hoursToReward + "h): Energy +" + finalEnergyGain
                    + " (x" + energyMult + "), Happiness +" + finalHappyGain
                    + " (x" + happyMult + ")");
        }

        // Automatic wake up at 8:00 AM
        if (pet.getCurrentGameHour() >= GameConfig.HOUR_WAKE_UP &&
                pet.getCurrentGameHour() < (GameConfig.HOUR_WAKE_UP + 1.0)) {

            System.out.println(pet.getName() + " automatically woke up at 8:00 AM.");
            pet.setPassedEightAM(true);
            wakeUp(pet);
        }
    }

    /**
     * Helper that transitions the pet from asleep to the awake state,
     * if the {@link AwakeState} is registered in the {@link StateRegistry}.
     *
     * @param pet the pet to wake
     */
    private void wakeUp(PetModel pet) {
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
        if (awakeState != null) {
            pet.changeState(awakeState);
        }
    }

    /**
     * Returns the canonical name of this state.
     *
     * @return {@link #STATE_NAME}
     */
    @Override
    public String getStateName() {
        return STATE_NAME;
    }

    @Override
    public double getTimeScale() {
        return GameConfig.TIMESCALE_SLEEP; // Returns 2.0
    }

    @Override
    public boolean canSleep() {
        return false; // Cannot start sleeping if already asleep
    }

    @Override
    public String getSoundName() {
        return "snoring";
    }
}