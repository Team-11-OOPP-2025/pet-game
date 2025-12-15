package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.ui.MiniGameView;
import javafx.scene.layout.Pane;

/**
 * Controller responsible for managing minigame sessions.
 * <p>
 * It interacts with the {@link MinigameRegistry} to select a game,
 * initializes the selected {@link Minigame}, and wraps it in a {@link MiniGameView}.
 * </p>
 */
public class MiniGameController {

    /**
     * Starts a new random minigame session.
     *
     * @param pet      the pet model that plays the game
     * @param onFinish callback to execute when the game finishes (e.g. close the UI)
     * @return a JavaFX Pane containing the game UI, or {@code null} if no games are available
     */
    public Pane startRandomGame(PetModel pet, Runnable onFinish) {
        Minigame game = MinigameRegistry.getInstance().getRandomGame();
        
        if (game != null) {
            game.initialize(pet, onFinish);
            return new MiniGameView(game);
        }
        
        return null;
    }
}