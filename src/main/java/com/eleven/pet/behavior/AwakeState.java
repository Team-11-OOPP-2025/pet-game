package com.eleven.pet.behavior;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.model.items.Item;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AwakeState implements PetState {
    public static final String STATE_NAME = "AWAKE";

    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        return item.use(pet);
    }

    @Override
    public void handlePlay(PetModel pet) {
        if (pet.canPlayMinigame()) {
            pet.playRandomMinigame();
        } else {
            System.out.println(pet.getName() + " is too tired or hungry to play right now.");
        }
    }

    @Override
    public void handleSleep(PetModel pet) {
        pet.performSleep();
    }

    @Override
    public void handleClean(PetModel pet) {
        // Simple cleaning effect: improve cleanliness and a bit of happiness
        pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, 20);
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, 5);
        System.out.println(pet.getName() + " has been cleaned.");
    }

    @Override
    public void onTick(PetModel pet) {
        // Check sleep cycle when in awake state
        if (pet.getClock() != null) {
            double currentHour = (pet.getClock().getGameTime() / GameConfig.DAY_LENGTH_SECONDS) * 24.0;

            // Check if pet slept at 8 AM
            if (currentHour >= 8.0 && currentHour < 20.0 && !pet.isPassedEightAM()) {
                if (!pet.hasSleptThisNight()) {
                    applyMissedSleepPenalty(pet);
                }
                pet.setPassedEightAM(true);
            }

            // Reset sleep flag at 20:00 (sleep window starts)
            if ((currentHour >= 20.0 || currentHour < 8.0) && pet.isPassedEightAM()) {
                pet.resetSleepFlag();
                pet.setPassedEightAM(false);
            }
        }
    }

    private void applyMissedSleepPenalty(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -30);
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, -20);
    }

    @Override
    public String getStateName() {
        return STATE_NAME;
    }

    @Override
    public void onEnter(PetModel pet) {
        System.out.println(pet.getName() + " is now awake.");
    }

    @Override
    public void onExit(PetModel pet) {
        System.out.println(pet.getName() + " is leaving the awake state.");
    }
}
