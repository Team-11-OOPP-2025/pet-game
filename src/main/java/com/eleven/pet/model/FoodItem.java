package com.eleven.pet.model;

import java.util.Objects;

public class FoodItem implements Consumable {
    private final int id;
    private final String name;
    private final int nutritionalValue;

    public FoodItem(int id, String name, int nutritionalValue) {
        this.id = id;
        this.name = name;
        this.nutritionalValue = nutritionalValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    public int getNutritionalValue() {
        return nutritionalValue;
    }

    @Override
    public void applyEffect(PetModel pet) {
        // TODO: Implement food item effect on pet
    }

    // Equals and hashCode based on id for proper comparison in collections
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FoodItem foodItem = (FoodItem) o;
        return Objects.equals(id, foodItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
