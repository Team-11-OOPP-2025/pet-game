package com.eleven.pet.model;

import java.util.Random;

public class GuessingGame implements Minigame {
    private static final int MAX_NUMBER = 10;
    private static final int HAPPINESS_WIN = 20;
    private static final int HAPPINESS_LOSE = -5;
    private final Random random = new Random();
    
    @Override
    public String getName() {
        return "Number Guessing Game";
    }
    
    @Override
    public MinigameResult play(PetModel pet) {
        // Simple implementation - in real game this would be interactive
        int target = random.nextInt(MAX_NUMBER) + 1;
        int guess = random.nextInt(MAX_NUMBER) + 1;
        
        boolean won = (guess == target);
        int delta = won ? HAPPINESS_WIN : HAPPINESS_LOSE;
        String message = won 
            ? "Congratulations! You guessed " + target + " correctly!" 
            : "Sorry, the number was " + target + ". You guessed " + guess + ".";
        
        return new MinigameResult(won, delta, message);
    }
}
