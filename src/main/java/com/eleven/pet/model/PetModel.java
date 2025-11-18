package com.eleven.pet.model;

import com.eleven.pet.weather.WeatherSystem;

public class PetModel {
    private String name;
    private int Cleanliness;
    private int Sleepiness;
    private int foodCount;
    private int hunger;
    private int happiness;
    private boolean isAsleep;
    private WeatherSystem weatherSystem;

    public PetModel(String name){
     this.name = name;
    }

    public void clean(){

    }
    public void sleep(){

    }
    public void feed(){

    }
    public void play(){

    }
    public void replenishDailyFood(){

    }

    public WeatherSystem getWeatherSystem(){
        WeatherSystem res = null;
        return res;
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
        int sleep = 0;
        return sleep;
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
    public void getFoodCountProperty(){

    }
    public void getHappinessProperty(){

    }


}
