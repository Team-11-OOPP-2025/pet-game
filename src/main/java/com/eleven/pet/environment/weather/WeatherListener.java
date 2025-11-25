package com.eleven.pet.environment.weather;

public interface WeatherListener {
    void onWeatherChanged(WeatherState newState);
}
