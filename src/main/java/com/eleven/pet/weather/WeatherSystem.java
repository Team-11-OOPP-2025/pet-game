package com.eleven.pet.weather;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Random;

public class WeatherSystem {

    private final ObjectProperty<WeatherState> currentState =
            new SimpleObjectProperty<>(new WeatherStates.SunnyState());

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
}
