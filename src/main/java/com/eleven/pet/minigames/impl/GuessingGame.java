package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.ui.MiniGameView;

import java.util.Random;

/**
 * A simple number guessing {@link com.eleven.pet.minigames.Minigame}.
 * <p>
 * The game picks a random secret number in a fixed range. The player attempts
 * to guess the number; on success the pet gains happiness, otherwise it loses
 * a small amount.
 */
public class GuessingGame implements Minigame {

    /**
     * Minimum value (inclusive) for the randomly chosen secret number.
     */
    private static final int MIN_NUMBER = 1;

    /**
     * Maximum value (inclusive) for the randomly chosen secret number.
     */
    private static final int MAX_NUMBER = 5;

    /**
     * Happiness gained when the player guesses the correct number.
     */
    private static final int WIN_HAPPINESS = 20;

    /**
     * Happiness lost when the player guesses the wrong number.
     */
    private static final int LOSE_HAPPINESS = -5;
    
    private int secretNumber;
    private final Random random;
    
    public GuessingGame() {
        this.random = new Random();
        generateNewNumber();
    }
    
    /**
     * @return the display name of this mini-game
     */
    @Override
    public String getName() {
        return "Guessing Game";
    }
    
    /**
     * Starts the guessing game UI for the given pet.
     * <p>
     * The actual game interaction is handled by {@link com.eleven.pet.minigames.ui.MiniGameView}.
     *
     * @param pet the {@link PetModel} currently playing the mini-game
     * @return currently {@code null}; the result is handled by the UI layer
     */
    @Override
    public MinigameResult play(PetModel pet) {
        MiniGameView.showMiniGame(pet);
        return null;
    }
    
    /**
     * Generates a new random secret number in the configured range.
     */
    public void generateNewNumber() {
        this.secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
    /**
     * Checks the provided guess against the current secret number and updates
     * the pet's happiness accordingly.
     *
     * @param guess the number guessed by the player
     * @param pet   the {@link PetModel} whose stats should be updated; may be {@code null}
     * @return a {@link MinigameResult} indicating success, happiness delta and a message
     */
    public MinigameResult checkGuess(int guess, PetModel pet) {
        boolean won = (guess == secretNumber);
        int happinessDelta = won ? WIN_HAPPINESS : LOSE_HAPPINESS;
        String message;
        
        if (won) {
            message = String.format("Correct! The number was %d. Happiness +%d", secretNumber, WIN_HAPPINESS);
            if (pet != null && pet.getStats() != null) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, WIN_HAPPINESS);
            }
        } else {
            message = String.format("Wrong! The number was %d. Happiness %d", secretNumber, LOSE_HAPPINESS);
            if (pet != null && pet.getStats() != null) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, LOSE_HAPPINESS);
            }
        }
        
        return new MinigameResult(won, happinessDelta, message);
    }
    
    /**
     * @return the minimum possible number that can be guessed
     */
    public int getMinNumber() {
        return MIN_NUMBER;
    }
    
    /**
     * @return the maximum possible number that can be guessed
     */
    public int getMaxNumber() {
        return MAX_NUMBER;
    }
}
