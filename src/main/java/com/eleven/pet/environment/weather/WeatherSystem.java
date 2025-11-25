package com.eleven.pet.environment.weather;

import java.util.Random;

import com.eleven.pet.environment.weather.states.SunnyState;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class WeatherSystem {

    private final ObjectProperty<WeatherState> currentState =
            new SimpleObjectProperty<>(new SunnyState());

    private final Random random = new Random();

    public WeatherSystem() {}

    /** Randomly changes the weather state (example implementation). */
    public void updateWeather() {

    }

    /** Returns current weather state (traditional getter). */
    public WeatherState getCurrentState() {
        return currentState.get();
    }

    /** Returns the property for JavaFX UI binding. */
    public ReadOnlyObjectProperty<WeatherState> currentStateProperty() {
        return currentState;
    }
    
    /** Alias for currentStateProperty() to match UML. */
    public ReadOnlyObjectProperty<WeatherState> getWeatherProperty() {
        return currentState;
    }
}
