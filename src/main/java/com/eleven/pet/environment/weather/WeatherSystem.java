package com.eleven.pet.environment.weather;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ServiceLoader;

import com.eleven.pet.environment.weather.states.SunnyState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class WeatherSystem {
    private final List<WeatherListener> listeners;
    private final ObjectProperty<WeatherState> currentWeather;
    private final List<WeatherState> availableStates;
    private final Random random;

    public WeatherSystem() {
        this.listeners = new ArrayList<>();
        this.availableStates = new ArrayList<>();
        this.random = new Random();
        
        loadWeatherStatesViaSPI();
        
        // Start with sunny if available, otherwise first loaded state
        WeatherState initial = availableStates.isEmpty() ? new SunnyState() : availableStates.get(0);
        this.currentWeather = new SimpleObjectProperty<>(initial);
    }
    
    private void loadWeatherStatesViaSPI() {
        ServiceLoader<WeatherState> loader = ServiceLoader.load(WeatherState.class);
        for (WeatherState state : loader) {
            availableStates.add(state);
            System.out.println("Loaded weather state: " + state.getName());
        }
        
        // Fallback if no states loaded via SPI
        if (availableStates.isEmpty()) {
            availableStates.add(new SunnyState());
        }
    }
    
    public void subscribe(WeatherListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void changeWeather() {
        if (availableStates.isEmpty()) {
            return;
        }
        
        // Pick a random weather state
        WeatherState newWeather = availableStates.get(random.nextInt(availableStates.size()));
        
        if (newWeather != currentWeather.get()) {
            currentWeather.set(newWeather);
            
            // Notify all listeners
            for (WeatherListener listener : listeners) {
                listener.onWeatherChanged(newWeather);
            }
            
            System.out.println("Weather changed to: " + newWeather.getName());
        }
    }

    public ReadOnlyObjectProperty<WeatherState> getWeatherProperty() {
        return currentWeather;
    }
    
    public WeatherState getCurrentWeather() {
        return currentWeather.get();
    }
}
