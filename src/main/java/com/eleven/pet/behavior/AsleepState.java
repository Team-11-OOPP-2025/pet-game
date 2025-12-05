package com.eleven.pet.behavior;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.model.items.Item;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "ASLEEP";

    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        // TODO: Implement consume behavior for asleep state
        return false;
    }

    @Override
    public void handlePlay(PetModel pet) {
        // TODO: Implement play behavior for asleep state
    }

    @Override
    public void handleSleep(PetModel pet) {
        // TODO: Implement sleep behavior for asleep state
    }

    @Override
    public void handleClean(PetModel pet) {
        // TODO: Implement clean behavior for asleep state
    }

    @Override
    public void onTick(PetModel pet) {
        // Auto-wake up at 8:00 AM when sleeping
        if (pet.getGameClock() != null && pet.isSleepingWithTimeAcceleration()) {
            double gameTime = pet.getGameClock().getGameTime();
            double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;
            double hour = normalizedTime * 24.0;

            // Wake up at 8:00 AM
            if (hour >= 8.0 && hour < 9.0 && !pet.hasPassedEightAM()) {
                pet.wakeUp();
                pet.setPassedEightAM(true);
                System.out.println(pet.getName() + " automatically woke up at 8:00 AM.");
            }

            // Reset the flag after 9 AM or before 8 AM to allow next day's wake-up
            if (hour >= 9.0 || hour < 8.0) {
                if (pet.hasPassedEightAM()) {
                    pet.setPassedEightAM(false);
                }
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
        // Apply sleep rewards when entering sleep state
        pet.getStats().modifyStat("energy", 40);
        pet.getStats().modifyStat("happiness", 20);
        pet.setSleptThisNight(true);
        System.out.println(pet.getName() + " had a good night's sleep! Energy and happiness restored.");
    }

    @Override
    public void onExit(PetModel pet) {
        //Todo: Implement onExit behavior for asleep state
    }

}
