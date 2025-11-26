package com.eleven.pet.state;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.model.Consumable;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AwakeState implements PetState {
    public static final String STATE_NAME = "awake";
    
    @Override
    public boolean handleConsume(PetModel pet, Consumable item) {
        item.applyEffect(pet);
        System.out.println(pet.getName() + " consumed " + item.getName());
        return true;
    }
    
    @Override
    public void handlePlay(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, GameConfig.PLAY_HAPPINESS_BOOST);
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -5);
        System.out.println(pet.getName() + " is playing!");
    }
    
    @Override
    public void handleSleep(PetModel pet) {
        if (pet.getStats().getStat(PetStats.STAT_ENERGY).get() <= GameConfig.SLEEP_ENERGY_THRESHOLD) {
            pet.changeState(new AsleepState());
            pet.setSleepStartTime(System.currentTimeMillis());
            System.out.println(pet.getName() + " is going to sleep...");
        } else {
            System.out.println(pet.getName() + " is not tired enough to sleep.");
        }
    }
    
    @Override
    public void handleClean(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, GameConfig.CLEAN_CLEANLINESS_RESTORE);
        System.out.println(pet.getName() + " is now clean!");
    }
    
    @Override
    public void onTick(PetModel pet) {
        // Decay stats while awake
        pet.getStats().modifyStat(PetStats.STAT_HUNGER, (int)(-GameConfig.HUNGER_DECAY_RATE));
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, (int)(-GameConfig.ENERGY_DECAY_RATE));
        pet.getStats().modifyStat(PetStats.STAT_CLEANLINESS, (int)(-GameConfig.CLEANLINESS_DECAY_RATE));
        
        // Update derived happiness
        pet.getStats().calculateDerivedHappiness();
    }
    
    @Override
    public String getStateName() {
        return STATE_NAME;
    }
}
