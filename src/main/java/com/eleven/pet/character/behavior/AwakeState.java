package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.inventory.Item;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AwakeState implements PetState {
    public static final String STATE_NAME = "AWAKE";


    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        if (pet.getInventory().remove(item, 1)) {
            System.out.println(pet.getName() + " is consuming " + item.name() + ".");
            return item.use(pet);
        }
        System.out.println(item.name() + " is not available in inventory.");
        return false;
    }

    @Override
    public void handlePlay(PetModel pet) {
        if (pet.canPlayMinigame()) {
            pet.playRandomMinigame();
            return;
        }
        System.out.println(pet.getName() + " is too tired or hungry to play right now.");
    }

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

    @Override
    public void handleClean(PetModel pet) {
        // Simple cleaning effect: improve cleanliness and a bit of happiness
        pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, 20);
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, 5);
        System.out.println(pet.getName() + " has been cleaned.");
    }

    @Override
    public void onTick(PetModel pet, double timeDelta) {
        if (pet.getClock() == null) return;

        double currentHour = pet.getCurrentGameHour();
        pet.applyStatDecay(pet, timeDelta);

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

    private void applyMissedSleepPenalty(PetModel pet) {
        System.out.println(pet.getName() + " stayed up all night! Penalty applied.");
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -GameConfig.MISSED_SLEEP_ENERGY_PENALTY);
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, -GameConfig.MISSED_SLEEP_HAPPINESS_PENALTY);
    }

    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}
