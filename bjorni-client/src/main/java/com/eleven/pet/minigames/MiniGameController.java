package com.eleven.pet.minigames;

/**
 * Controller responsible for managing minigame sessions.
 */
public class MiniGameController {

    /**
     * Starts a new random minigame session.
     *
     * @return Random {@link Minigame} instance
     */
    public Minigame startRandomGame() {
        return MinigameRegistry.getInstance().getRandomGame();
    }
}