package com.eleven.pet.behavior;

import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.Item;
import com.google.auto.service.AutoService;

@AutoService(PetState.class)
public class AsleepState implements PetState {
    public static final String STATE_NAME = "asleep";
    
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
        // Sleep state onTick - pet is sleeping, no actions needed per tick
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
        return;
    }

}
