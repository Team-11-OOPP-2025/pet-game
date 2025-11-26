package com.eleven.pet.model;

public class FoodItem implements Consumable {
    private final String name;
    private final int nutritionalValue;
    
    public FoodItem(String name, int nutritionalValue) {
        this.name = name;
        this.nutritionalValue = nutritionalValue;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    public int getNutritionalValue() {
        return nutritionalValue;
    }
    
    @Override
    public void applyEffect(PetModel pet) {
        pet.getStats().modifyStat(PetStats.STAT_HUNGER, nutritionalValue);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof FoodItem)) return false;
        FoodItem other = (FoodItem) obj;
        return name.equals(other.name) && nutritionalValue == other.nutritionalValue;
    }
    
    @Override
    public int hashCode() {
        return 31 * name.hashCode() + nutritionalValue;
    }
}
