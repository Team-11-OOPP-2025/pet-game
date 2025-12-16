package com.eleven.pet.daily_reward;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;

import java.util.Random;

/**
 * Logical representation of a reward chest.
 * <p>
 * A chest randomly selects an {@link Item} and quantity upon creation
 * and can be opened once to grant the reward to a {@link PetModel}.
 * </p>
 */
public class Chest {
    private static final Random random = new Random();
    private Item item;
    private int quantity;
    private boolean isOpened;
        
    /**
     * Creates a new unopened chest with a randomly generated reward.
     */
    public Chest() {
        this.isOpened = false;
        generateReward();
    }

    /**
     * Randomly selects an {@link Item} and quantity for this chest.
     * Quantity is currently an integer between 1 and 3 (inclusive).
     */
    private void generateReward() {
        this.item = ItemRegistry.getRandomItem();
        // Generate a random quantity between 1 and 3
        this.quantity = random.nextInt(3) + 1;
    }

    /**
     * Opens the chest and grants its reward to the given pet.
     * <p>
     * This method has no effect if the chest is already opened or
     * if no item was generated.
     * </p>
     *
     * @param pet the pet model whose inventory will receive the reward
     */
    public void open(PetModel pet) {
        if (!isOpened && item != null) {
            pet.addToInventory(item, quantity);
            isOpened = true;
            System.out.println("Chest opened! Received " + quantity + "x " + item.name());
        }
    }

    /**
     * Returns the item contained in this chest.
     *
     * @return the rewarded {@link Item}, or {@code null} if none
     */
    public Item getItem() {
        return item;
    }

    /**
     * Returns the quantity of the contained item.
     *
     * @return item quantity, typically between 1 and 3
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Indicates whether this chest has already been opened.
     *
     * @return {@code true} if the chest was opened, {@code false} otherwise
     */
    public boolean isOpened() {
        return isOpened;
    }
}