package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;

public class PetFactory {
    
    public static PetModel createNewPet(String name, WeatherSystem weatherSystem, GameClock clock) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Pet name cannot be null or empty");
        }
        if (weatherSystem == null) {
            throw new IllegalArgumentException("WeatherSystem cannot be null");
        }
        if (clock == null) {
            throw new IllegalArgumentException("GameClock cannot be null");
        }
        
        PetModel pet = new PetModel(name, weatherSystem, clock);
        System.out.println("✓ Created new pet: " + name);
        return pet;
    }
}
