package com.eleven.pet.minigames;

/**
 * Represents the outcome of a minigame.
 *
 * @param won             whether the minigame was won
 * @param happinessDelta  the change to the pet's happiness as a result of the game
 * @param message         a user-facing message describing the result
 */
public record MinigameResult(boolean won, int happinessDelta, String message) {
}
