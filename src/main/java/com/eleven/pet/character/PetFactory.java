package com.eleven.pet.character;

import com.eleven.pet.character.behavior.PetDefinition;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;

/**
 * Factory for creating new PetModel instances with default state.
 */
public final class PetFactory {
    /**
     * Creates a new PetModel with the given name, weather system, and game clock.
     *
     * @param name          The name of the sprites.
     * @param weatherSystem The weather system to associate with the sprites.
     * @param clock         The game clock to associate with the sprites.
     * @return A new PetModel instance.
     */
    public static PetModel createNewPet(String name, WeatherSystem weatherSystem, GameClock clock, PetDefinition petDefinition) {
        return new PetModel(name, weatherSystem, clock, petDefinition);
    }
}
