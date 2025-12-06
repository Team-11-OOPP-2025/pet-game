package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.ui.MiniGameView;

import java.util.Random;

public class GuessingGame implements Minigame {
    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_HAPPINESS = 20;
    private static final int LOSE_HAPPINESS = -5;
    
    private int secretNumber;
    private Random random;
    
    public GuessingGame() {
        this.random = new Random();
        generateNewNumber();
    }
    
    @Override
    public String getName() {
        return "Guessing Game";
    }
    
    @Override
    public MinigameResult play(PetModel pet) {
        MiniGameView.showMiniGame(pet);
        return null;
    }
    
    public void generateNewNumber() {
        this.secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
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
    
    public int getMinNumber() {
        return MIN_NUMBER;
    }
    
    public int getMaxNumber() {
        return MAX_NUMBER;
    }
}
