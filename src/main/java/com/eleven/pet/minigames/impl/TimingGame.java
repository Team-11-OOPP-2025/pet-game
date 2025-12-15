package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;

/**
 * Logic-only implementation of the Timing Game.
 * Decoupled from JavaFX UI code to allow embedding in the unified MiniGameView.
 */
public class TimingGame implements Minigame {

    public static final double TARGET_MIN = 0.40; // 40%
    public static final double TARGET_MAX = 0.60; // 60%
    public static final double FILL_SPEED = 0.02; // Progress per tick (approx 60fps)
    
    private static final int WIN_HAPPINESS = 20;
    private static final int LOSE_HAPPINESS = -5;

    @Override
    public String getName() {
        return "Timing Challenge";
    }

    @Override
    public MinigameResult play(PetModel pet) {
        // This method is for standalone execution; in TV mode, controller uses checkResult directly.
        return null; 
    }

    /**
     * Checks if the stopped progress is within the winning zone.
     * @param progress value between 0.0 and 1.0
     * @param pet pet model to update
     * @return Result of the game
     */
    public MinigameResult checkResult(double progress, PetModel pet) {
        boolean won = progress >= TARGET_MIN && progress <= TARGET_MAX;
        int happinessDelta = won ? WIN_HAPPINESS : LOSE_HAPPINESS;
        String message;

        if (won) {
            message = "PERFECT! The Pet is happy!";
            if (pet != null) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, WIN_HAPPINESS);
            }
        } else {
            message = "Missed! The Pet is disappointed.";
            if (pet != null) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, LOSE_HAPPINESS);
            }
        }

        return new MinigameResult(won, happinessDelta, message);
    }
}