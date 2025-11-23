package com.eleven.pet.environment.weather;

public class RainyState implements WeatherState {
    @Override
    public String getName() {
        return "Rainy";
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