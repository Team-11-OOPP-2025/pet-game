package com.eleven.pet.character;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;

/**
 * Factory for creating new {@link PetModel} instances with default state.
 *
 * <p>This class centralizes construction logic so that callers do not need
 * to know about default {@link PetDefinition} or initialization details.</p>
 */
public final class PetFactory {

    private PetFactory() {
        // utility class; prevent instantiation
    }

    /**
     * Creates a new {@link PetModel} with the given name, weather system, and game clock.
     *
     * @param name          the name of the pet
     * @param weatherSystem the weather system to associate with the pet (may be {@code null})
     * @param clock         the game clock to associate with the pet (may be {@code null})
     * @return a new {@link PetModel} instance
     */
    public static PetModel createNewPet(String name, WeatherSystem weatherSystem, GameClock clock) {
        return new PetModel(name, weatherSystem, clock);
    }
}
