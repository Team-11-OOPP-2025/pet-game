package com.eleven.pet.weather;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;

public class WeatherSystem implements WeatherState {
    private ObjectProperty<WeatherState> currentState;

    public void updateWeather(){

    }

    public WeatherState getCurrentState(){
        WeatherState state = null;
        return state;
    }
    public ReadOnlyObjectProperty<WeatherState> getCurrentWeatherProperty(){
        WeatherState state = null;
        return (ReadOnlyObjectProperty<WeatherState>) state;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public double getHappinessModifier() {
        return 0;
    }
}
