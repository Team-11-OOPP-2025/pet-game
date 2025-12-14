package com.eleven.pet.daily_reward;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;

import java.util.Random;

public class Chest {
    private static final Random random = new Random();
    private Item item;
    private int quantity;
    private boolean isOpened;
        
    public Chest() {
        this.isOpened = false;
        generateReward();
    }

    private void generateReward() {
        this.item = ItemRegistry.getRandomItem();
        // Generate a random quantity between 1 and 3
        this.quantity = random.nextInt(3) + 1;
    }

    public void open(PetModel pet) {
        if (!isOpened && item != null) {
            pet.addToInventory(item, quantity);
            isOpened = true;
            System.out.println("Chest opened! Received " + quantity + "x " + item.name());
        }
    }

    public Item getItem() {
        return item;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isOpened() {
        return isOpened;
    }
}