package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.items.FoodItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetFactoryTest {

    @Test
    void testFactoryCreatesValidPet() {
        WeatherSystem weather = new WeatherSystem();
        GameClock clock = new GameClock();

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertNotNull(pet);
    }

    @Test
    void testDefaultInventory() {
        WeatherSystem weather = new WeatherSystem();
        GameClock clock = new GameClock();

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertEquals(3, pet.getInventory().getAmount(FoodItem.class));
    }

    @Test
    void testDependenciesInjected() {
        WeatherSystem weather = new WeatherSystem();
        GameClock clock = new GameClock();

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertNotNull(pet.getClock());
        assertNotNull(pet.getWeatherSystem());
    }
}
