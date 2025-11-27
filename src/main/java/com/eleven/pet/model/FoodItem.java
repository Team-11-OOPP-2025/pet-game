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
        // TODO: Implement food item effect on pet
    }
}
