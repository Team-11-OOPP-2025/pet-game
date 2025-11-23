package com.eleven.pet.environment.weather;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherSystem {
    private final List<WeatherListener> listeners = new ArrayList<>();
    private final ObjectProperty<WeatherState> currentWeather = new SimpleObjectProperty<>();
    private final List<WeatherState> availableStates = new ArrayList<>();
    private final Random random = new Random();

    public WeatherSystem() {
        availableStates.add(new SunnyState());
        availableStates.add(new RainyState());
        availableStates.add(new CloudyState());

    }

    public void subscribe(WeatherListener listener) {
        listeners.add(listener);
    }

    public void changeWeather() {
        WeatherState newState = availableStates.get(random.nextInt(availableStates.size()));
        currentWeather.set(newState);

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
