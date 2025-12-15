// src/main/java/com/eleven/pet/minigames/Minigame.java
package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;
import javafx.scene.layout.Pane;

/**
 * Common contract for all minigames that can be played with a pet.
 * <p>
 * Follows the MVC pattern where the implementation class acts as the Controller,
 * managing its own internal Model and providing a View via {@link #getView()}.
 * </p>
 */
public interface Minigame {

    /**
     * Returns the display name of the minigame.
     *
     * @return the human-readable name of the game
     */
    String getName();

    /**
     * Initializes the game with the necessary context.
     *
     * @param pet      the pet model to interact with (e.g. modify stats)
     * @param onFinish callback to invoke when the game is finished (to exit the view)
     */
    void initialize(PetModel pet, Runnable onFinish);

    /**
     * Retrieves the visual interface for this minigame.
     * <p>
     * This method should return a fully constructed JavaFX Pane containing
     * the game's UI. The game controller (this class) should attach necessary
     * event listeners to the view components.
     * </p>
     *
     * @return the root Pane of the minigame UI
     */
    Pane getView();
}