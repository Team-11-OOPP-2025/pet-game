package com.eleven.pet.environment.weather;

/**
 * Listener for weather changes fired by {@link WeatherSystem}.
 */
public interface WeatherListener  {
    /**
     * Called whenever the {@link WeatherSystem} switches to a new
     * {@link WeatherState}.
     *
     * @param newState the newly active weather state
     */
    void onWeatherChange(WeatherState newState);
}
