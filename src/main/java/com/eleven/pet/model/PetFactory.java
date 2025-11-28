package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.items.FoodItem;
import com.eleven.pet.model.items.MedicineItem;

/**
 * Factory for creating new PetModel instances with default state.
 */
public final class PetFactory {

    private PetFactory() {
        // no instances
    }

    public static PetModel createNewPet(WeatherSystem weatherSystem, GameClock clock) {
        PetModel pet = new PetModel(weatherSystem, clock);

        // Initialize default inventory items (e.g. 3 Apples)
        pet.getInventory().addItem(new FoodItem("apple", 10, 10), 3);

        // Optionally set up default stats here:
        pet.getStats().registerStat("hunger", 50);
        pet.getStats().registerStat("happiness", 50);
        pet.getStats().registerStat("energy", 50);

        return pet;
    }
}
