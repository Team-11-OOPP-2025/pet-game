package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;

import java.util.Random;

/**
 * A simple number guessing {@link com.eleven.pet.minigames.Minigame}.
 * <p>
 * The game picks a random secret number in a fixed range. The player attempts
 * to guess the number; on success the pet gains happiness, otherwise it loses
 * a small amount.
 * </p>
 */
public class GuessingGame implements Minigame {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_HAPPINESS = 20;
    private static final int LOSE_HAPPINESS = -5;
    
    private int secretNumber;
    private final Random random;
    
    /**
     * Creates a new {@code GuessingGame} instance and generates
     * the initial secret number.
     */
    public GuessingGame() {
        this.random = new Random();
        generateNewNumber();
    }
    
    /**
     * Returns the display name of this minigame.
     *
     * @return the name of the minigame
     */
    @Override
    public String getName() {
        return "Guessing Game";
    }
    
    /**
     * Plays the minigame using automated logic.
     * <p>
     * In the current implementation this method is primarily a placeholder,
     * since the game is intended to be driven via the UI/Controller layer.
     * </p>
     *
     * @param pet the {@link PetModel} whose stats may be affected
     * @return {@code null} in the current implementation
     */
    @Override
    public MinigameResult play(PetModel pet) {
        // UI invocation is now handled by the Controller/View layer directly
        // to support both embedded (TV) and potential future standalone modes.
        // For the "Play" button shortcut, we could return a placeholder result or log.
        System.out.println("Mini-games should be played via the TV!");
        return null;
    }
    
    /**
     * Generates a new random secret number within the configured range.
     * <p>
     * This should be called when starting a new round.
     * </p>
     */
    public void generateNewNumber() {
        this.secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
    /**
     * Checks the player's guess against the current secret number and updates
     * the pet's happiness accordingly.
     *
     * @param guess the guessed number
     * @param pet   the {@link PetModel} to update; may be {@code null}
     * @return a {@link MinigameResult} describing win/loss, happiness delta,
     *         and a user-facing message
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
     * Returns the minimum possible secret number (inclusive).
     *
     * @return the minimum number that can be generated
     */
    public int getMinNumber() {
        return MIN_NUMBER;
    }
    
    /**
     * Returns the maximum possible secret number (inclusive).
     *
     * @return the maximum number that can be generated
     */
    public int getMaxNumber() {
        return MAX_NUMBER;
    }
}