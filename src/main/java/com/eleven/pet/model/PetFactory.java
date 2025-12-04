package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;

/**
 * Factory for creating new PetModel instances with default state.
 */
public final class PetFactory {
    public static PetModel createNewPet(String name, WeatherSystem weatherSystem, GameClock clock) {
        return new PetModel(name, weatherSystem, clock);
    }
}
