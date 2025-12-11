package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.ui.MiniGameView;
import com.eleven.pet.minigames.MiniGameController;

import javafx.application.Platform;

public class TimingGame implements Minigame {

    // Game Configuration / Rules
    private static final double TARGET_MIN = 0.40; // 40%
    private static final double TARGET_MAX = 0.60; // 60%
    private static final double FILL_SPEED = 0.01; // Progress per frame

    @Override
    public String getName() {
        return "Timing Challenge";
    }

    @Override
    public MinigameResult play(PetModel pet) {
        // Wrapper to hold result for thread safety
        MinigameResult[] resultHolder = new MinigameResult[1];

        // Ensure UI runs on FX Thread
        if (Platform.isFxApplicationThread()) {
            resultHolder[0] = startSession(pet);
        } else {
            Platform.runLater(() -> {
                resultHolder[0] = startSession(pet);
            });
            // Note: In a real app, if calling from background thread, 
            // you might need synchronization to wait for this result.
        }

        return resultHolder[0];
    }

    private MinigameResult startSession(PetModel pet) {
        // 1. Create View
        MiniGameView view = new MiniGameView(getName(), TARGET_MIN, TARGET_MAX);

        // 2. Create Controller (injects Model, View, and Context)
        MiniGameController controller = new MiniGameController(this, view, pet);

        // 3. Run Game (Blocks until finished due to showAndWait in View)
        controller.startGame();

        // 4. Return Result
        return controller.getResult();
    }

    // Getters for the Controller to access Rules
    public double getTargetMin() { return TARGET_MIN; }
    public double getTargetMax() { return TARGET_MAX; }
    public double getFillSpeed() { return FILL_SPEED; }
}