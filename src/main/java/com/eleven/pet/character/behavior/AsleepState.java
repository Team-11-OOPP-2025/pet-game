package com.eleven.pet.character.behavior;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.minigames.MinigameResult;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "ASLEEP";

    @Override
    public boolean handleConsume(PetModel pet, Item item) {
        // Pet is asleep; ignore eating actions
        System.out.println(pet.getName() + " is asleep and cannot eat right now.");
        return false;
    }

    @Override
    public MinigameResult handlePlay(PetModel pet) {
        System.out.println(pet.getName() + " is asleep and cannot play right now.");
        return null;
    }

    @Override
    public void handleSleep(PetModel pet) {
        // Manual Wake Up
        System.out.println("Player woke up " + pet.getName() + " manually.");
        wakeUp(pet);
    }

    @Override
    public void handleClean(PetModel pet) {
        System.out.println(pet.getName() + " is asleep and cannot be cleaned right now.");
    }

    @Override
    public void onTick(PetModel pet, double timeDelta) {
        double newDuration = pet.getCurrentSleepDuration() + timeDelta;
        pet.setCurrentSleepDuration(newDuration);

        // Calculate total hours slept
        double secondsPerGameHour = GameConfig.DAY_LENGTH_SECONDS / 24.0;
        int totalHoursSlept = (int) (newDuration / secondsPerGameHour);

        // Reward energy and happiness for each hour slept
        int hoursToReward = totalHoursSlept - pet.getHoursSleptRewardCount();

        // Safety check: Prevent massive spikes if logic desyncs
        if (hoursToReward > 10 && pet.getHoursSleptRewardCount() == 0) {
            pet.setHoursSleptRewardCount(totalHoursSlept);
            System.out.println("Synced sleep reward counter (prevented massive load spike).");
        }

        if (hoursToReward > 0) {
            // Hämta multiplikatorer för stats (t.ex. från potions)
            double energyMult = pet.getStatMultiplier(PetStats.STAT_ENERGY);
            double happyMult = pet.getStatMultiplier(PetStats.STAT_HAPPINESS);

            // Beräkna grundvärden baserat på konfiguration
            int baseEnergy = hoursToReward * GameConfig.SLEEP_ENERGY_PER_HOUR; // Default 5
            int baseHappy = hoursToReward * GameConfig.SLEEP_HAPPINESS_PER_HOUR; // Default 2

            // Applicera multiplikatorn
            int finalEnergyGain = (int) (baseEnergy * energyMult);
            int finalHappyGain = (int) (baseHappy * happyMult);

            // Uppdatera stats
            pet.getStats().modifyStat(PetStats.STAT_ENERGY, finalEnergyGain);
            pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, finalHappyGain);

            pet.setHoursSleptRewardCount(pet.getHoursSleptRewardCount() + hoursToReward);

            System.out.println("Sömn (" + hoursToReward + "h): Energy +" + finalEnergyGain 
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

    private void wakeUp(PetModel pet) {
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
        if (awakeState != null) {
            pet.changeState(awakeState);
        }
    }

    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}