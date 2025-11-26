package com.eleven.pet.model;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;

public class PetFactory {
    public static PetModel createNewPet(String name, WeatherSystem weatherSystem, GameClock clock) {
        PetModel pet = new PetModel(name, weatherSystem, clock);
        
        // Subscribe pet to systems
        clock.subscribe(pet);
        weatherSystem.subscribe(pet);
        
        return pet;
    }
}
