package com.eleven.pet.weather;

public class WeatherStates {

    public static class SunnyState implements WeatherState {
        public String getName() { return "Sunny"; }
        public double getHappinessModifier() { return 0; }
    }

    public static class CloudyState implements WeatherState {
        public String getName() { return "Cloudy"; }
        public double getHappinessModifier() { return 0; }
    }

    public static class RainyState implements WeatherState {
        public String getName() { return "Rainy"; }
        public double getHappinessModifier() { return 0; }
    }

}
