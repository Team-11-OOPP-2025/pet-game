package com.eleven.pet.model;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PetFactoryTest {

    @Test
    void testFactoryCreatesValidPet() {
        WeatherSystem weather = new WeatherSystem("test");
        GameClock clock = new GameClock("test");

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertNotNull(pet);
    }

    @Test
    void testDefaultInventory() {
        WeatherSystem weather = new WeatherSystem("test");
        GameClock clock = new GameClock("test");

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertEquals(3, pet.getInventory().getItemCount("Apple"));
    }

    @Test
    void testDependenciesInjected() {
        WeatherSystem weather = new WeatherSystem("test");
        GameClock clock = new GameClock("test");

        PetModel pet = PetFactory.createNewPet(weather, clock);

        assertNotNull(pet.getClock());
        assertNotNull(pet.getWeatherSystem());
    }
}
