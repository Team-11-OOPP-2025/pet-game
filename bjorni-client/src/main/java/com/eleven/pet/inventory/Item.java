package com.eleven.pet.inventory;

import com.eleven.pet.character.PetModel;

/**
 * Base interface for all inventory items.
 * <p>
 * Implementations define how an item is displayed and how it affects
 * a {@link PetModel} when used.
 * </p>
 */
public interface Item {

    /**
     * Returns the unique identifier of this item.
     *
     * @return item ID
     */
    int id();

    /**
     * Returns the display name of this item.
     *
     * @return item name
     */
    String name();

    /**
     * Returns the image file name used for rendering this item.
     *
     * @return image file name without extension
     */
    String imageFileName();

    /**
     * Returns a human-readable description of this item.
     *
     * @return item description
     */
    String description();

    /**
     * Returns the amount of stat restoration provided by this item, if applicable.
     *
     * @return restored stat amount (may be zero for non-restorative items)
     */
    int statsRestore();

    /**
     * Uses this item on the given pet.
     *
     * @param pet target pet model
     * @return {@code true} if the item was successfully applied
     */
    boolean use(PetModel pet);

    /**
     * Returns the sound name to be played when this item is used.
     *
     * @return sound name
     */
    default String getSoundName() {
        return null;
    }

}
