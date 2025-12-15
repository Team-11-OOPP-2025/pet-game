package com.eleven.pet.environment.weather;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

/**
 * Central manager for the game's weather.
 * <p>
 * It discovers all {@link WeatherState} implementations via {@link ServiceLoader},
 * tracks the current weather in a JavaFX {@link ObjectProperty}, and notifies
 * registered {@link WeatherListener listeners} when the weather changes.
 */
public class WeatherSystem {
    private final List<WeatherListener> listeners = new ArrayList<>();
    private final ObjectProperty<WeatherState> currentWeather = new SimpleObjectProperty<>();
    private final List<WeatherState> availableStates = new ArrayList<>();
    private final Random random = new Random();

    /**
     * Creates a new {@code WeatherSystem} and loads all available
     * {@link WeatherState} implementations using {@link ServiceLoader}.
     */
    public WeatherSystem() {
        ServiceLoader<WeatherState> loader = ServiceLoader.load(WeatherState.class);
        for (WeatherState ws : loader) {
            availableStates.add(ws);
        }
    }

    /**
     * Registers a {@link WeatherListener} that will be notified whenever
     * the weather changes.
     *
     * @param listener listener to register; must not be {@code null}
     */
    public void subscribe(WeatherListener listener) {
        listeners.add(listener);
    }

    /**
     * Randomly picks a new {@link WeatherState} from the discovered states,
     * updates the {@link #currentWeather} property and notifies all
     * subscribed {@link WeatherListener listeners}.
     */
    public void changeWeather() {
        WeatherState newState = availableStates.get(random.nextInt(availableStates.size()));
        currentWeather.set(newState);
        
        System.out.println("🌤️ Weather changed to: " + newState.getName());

        for (WeatherListener listener : listeners) {
            listener.onWeatherChange(newState);
        }
    }

    /**
     * Returns a read-only JavaFX property representing the current weather.
     * <p>
     * This can be used for UI bindings.
     *
     * @return read-only property for the current {@link WeatherState}
     */
    public ReadOnlyObjectProperty<WeatherState> getWeatherProperty() {
        return currentWeather;
    }

    /**
     * Returns the current {@link WeatherState}, or {@code null} if none
     * has been set yet.
     *
     * @return current weather state or {@code null}
     */
    public WeatherState getCurrentWeather() {
        return currentWeather.get();
    }
}
