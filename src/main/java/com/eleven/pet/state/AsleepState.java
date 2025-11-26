package com.eleven.pet.state;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "asleep";
    
    @Override
    public boolean handleConsume(PetModel pet, Consumable item) {
        System.out.println(pet.getName() + " is sleeping and cannot eat.");
        return false;
    }
    
    @Override
    public void handlePlay(PetModel pet) {
        System.out.println(pet.getName() + " is sleeping and cannot play.");
    }
    
    @Override
    public void handleSleep(PetModel pet) {
        System.out.println(pet.getName() + " is already sleeping.");
    }
    
    @Override
    public void handleClean(PetModel pet) {
        System.out.println(pet.getName() + " is sleeping and cannot be cleaned.");
    }
    
    @Override
    public void onTick(PetModel pet) {
        // Recover energy while asleep
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, (int)GameConfig.SLEEP_RECOVERY_RATE);
        
        // Check if should wake up
        if (pet.getStats().getStat(PetStats.STAT_ENERGY).get() >= GameConfig.WAKE_ENERGY_THRESHOLD) {
            long sleepDuration = System.currentTimeMillis() - pet.getSleepStartTime();
            if (sleepDuration >= GameConfig.SLEEP_DURATION_MS) {
                pet.wakeUp();
            }
        }
    }
    
    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}
