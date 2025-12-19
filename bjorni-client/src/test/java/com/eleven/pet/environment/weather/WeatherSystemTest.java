package com.eleven.pet.environment.weather;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style tests for the {@link WeatherSystem} and its {@link WeatherState}s.
 * <p>
 * Uses manual registration of weather states instead of {@code ServiceLoader}
 * to keep tests self-contained and deterministic.
 */
public class WeatherSystemTest {

    private WeatherSystem weatherSystem;

    /**
     * Initializes a new {@link WeatherSystem} instance and manually registers
     * a small set of {@link WeatherState} implementations for use in tests.
     *
     * @throws Exception if reflection-based access to the internal state list fails
     */
    @BeforeEach
    void setUp() throws Exception {
        weatherSystem = new WeatherSystem();

        // Manually add weather states for testing (ServiceLoader doesn't work in test context)
        Field availableStatesField = WeatherSystem.class.getDeclaredField("availableStates");
        availableStatesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<WeatherState> availableStates = (List<WeatherState>) availableStatesField.get(weatherSystem);
        availableStates.add(new SunnyState());
        availableStates.add(new RainyState());
        availableStates.add(new CloudyState());
    }

    /**
     * Verifies that calling {@link WeatherSystem#changeWeather()} always
     * results in a non-{@code null} current weather state.
     */
    @Test
    void testChangeWeather() {
        weatherSystem.changeWeather();
        assertNotNull(weatherSystem.getCurrentWeather(), "Current weather should not be null after change");
    }

    /**
     * Verifies that registered {@link WeatherListener}s are notified whenever
     * the weather changes and receive the new {@link WeatherState}.
     */
    @Test
    void testObserverNotification() {
        // Create a simple listener to track if it was called
        final boolean[] listenerCalled = {false};
        final WeatherState[] receivedState = {null};

        WeatherListener testListener = newState -> {
            listenerCalled[0] = true;
            receivedState[0] = newState;
        };

        weatherSystem.subscribe(testListener);
        weatherSystem.changeWeather();

        assertTrue(listenerCalled[0], "Listener should be called when weather changes");
        assertNotNull(receivedState[0], "Listener should receive a WeatherState");
    }

    /**
     * Verifies the numeric happiness modifier and user-facing name
     * for the {@link RainyState} weather implementation.
     */
    @Test
    void testRainyModifier() {
        RainyState rainyState = new RainyState();
        assertEquals(0.8, rainyState.getHappinessModifier(), 0.01,
                "Rainy weather should have 0.8 happiness modifier (makes pet sad)");
        assertEquals("Rainy", rainyState.getName(), "State name should be 'Rainy'");
    }

    /**
     * Verifies the numeric happiness modifier and user-facing name
     * for the {@link SunnyState} weather implementation.
     */
    @Test
    void testSunnyModifier() {
        SunnyState sunnyState = new SunnyState();
        assertEquals(1.2, sunnyState.getHappinessModifier(), 0.01,
                "Sunny weather should have 1.2 happiness modifier (makes pet happy)");
        assertEquals("Sunny", sunnyState.getName(), "State name should be 'Sunny'");
    }
}
