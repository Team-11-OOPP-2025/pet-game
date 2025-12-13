package com.eleven.pet.environment.weather;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

public class WeatherSystem {
    private final List<WeatherListener> listeners = new ArrayList<>();
    private final ObjectProperty<WeatherState> currentWeather = new SimpleObjectProperty<>();
    private final List<WeatherState> availableStates = new ArrayList<>();
    private final Random random = new Random();

    public WeatherSystem() {
        ServiceLoader<WeatherState> loader = ServiceLoader.load(WeatherState.class);
        for (WeatherState ws : loader) {
            availableStates.add(ws);
        }
    }

    public void subscribe(WeatherListener listener) {
        listeners.add(listener);
    }

    public void changeWeather() {
        WeatherState newState = availableStates.get(random.nextInt(availableStates.size()));
        currentWeather.set(newState);
        
        System.out.println("🌤️ Weather changed to: " + newState.getName());

        for (WeatherListener listener : listeners) {
            listener.onWeatherChange(newState);
        }
    }

    public ReadOnlyObjectProperty<WeatherState> getWeatherProperty() {
        return currentWeather;
    }

    public WeatherState getCurrentWeather() {
        return currentWeather.get();
    }
}
