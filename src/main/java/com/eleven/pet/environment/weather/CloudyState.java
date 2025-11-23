package com.eleven.pet.environment.weather;

public class CloudyState implements WeatherState {
    @Override
    public String getName() {
        return "Cloudy";
    }

    @Override
    public String getOverlayImageName() {
        return "";
    }

    @Override
    public double getOverlayOpacity() {
        return 0;
    }

    @Override
    public double getHappinessModifier() {
        return 0;
    }
}