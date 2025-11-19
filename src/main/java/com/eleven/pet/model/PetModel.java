package com.eleven.pet.model;

import java.util.Random;

import com.eleven.pet.time.GameClock;
import com.eleven.pet.weather.WeatherSystem;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class PetModel {
    private String name;
    private int Cleanliness;
    private int Sleepiness;
    private IntegerProperty foodCount;
    private int hunger;
    private int happiness;
    private boolean isAsleep;
    private WeatherSystem weatherSystem;
    private GameClock gameClock;
    private Random random;

    public PetModel(String name){
        this.name = name;
        this.gameClock = new GameClock();
        this.foodCount = new SimpleIntegerProperty(50); // Start with 50 food
        this.random = new Random();
    }

    public void clean(){

    }
    public void sleep(){

    }
    public void feed(){
        if (foodCount.get() > 0) {
            foodCount.set(foodCount.get() - 1);
            System.out.println("Fed pet! Remaining food: " + foodCount.get());
        } else {
            System.out.println("No food left!");
        }
    }
    public void play(){

    }
    public void replenishDailyFood(){
        // Add random amount between 3-8 food each day
        int newFood = 3 + random.nextInt(6); // Random from 3 to 8
        int currentFood = foodCount.get();
        int newTotal = Math.min(currentFood + newFood, 100); // Cap at 100
        foodCount.set(newTotal);
    }

    public WeatherSystem getWeatherSystem(){
        WeatherSystem res = null;
        return res;
    }
    
    public GameClock getGameClock(){
        return gameClock;
    }
    
    public int getCleanliness(){
        int clean = 0;
        return clean;
    }
    public int getSleepiness(){
        int sleep = 0;
        return sleep;
    }
    public int getHunger(){
        int sleep = 0;
        return sleep;
    }
    public int getHappiness(){
        int sleep = 0;
        return sleep;
    }
    public int getFoodCount(){
        return foodCount.get();
    }

    public IntegerProperty getFoodCountProperty(){
        return foodCount;
    }

    public void setHunger(int val){

    }
    public void setHappiness(int val){

    }
    public void setCleanliness(int val){

    }
    public void setFoodCount(int val){

    }
    public void setSleepiness(int val){

    }

    public void getSleepinessProperty(){

    }
    public void getCleanlinessProperty(){

    }
    public void getHungerProperty(){

    }
    public void getHappinessProperty(){

    }

}
