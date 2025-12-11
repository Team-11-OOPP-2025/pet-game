package com.eleven.pet.minigames;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.ui.MiniGameView;
import com.eleven.pet.minigames.impl.TimingGame;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MiniGameController {

    private final TimingGame model; // The game rules/configuration
    private final MiniGameView view;
    private final PetModel pet;
    
    private Timeline timeline;
    private double currentProgress = 0.0;
    private boolean isRunning = false;
    private MinigameResult result;

    public MiniGameController(TimingGame model, MiniGameView view, PetModel pet) {
        this.model = model;
        this.view = view;
        this.pet = pet;
        
        initialize();
    }

    private void initialize() {
        // Setup Game Loop
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> updateGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        // Bind View Events
        view.getStopButton().setOnAction(e -> handleStop());
        
        // Handle window close (X button)
        view.getStage().setOnCloseRequest(e -> {
            stopLoop();
            if (result == null) {
                result = new MinigameResult(false, -5, 
                    pet.getName() + " gave up on the game. (-5 happiness)");
            }
        });
    }

    public void startGame() {
        isRunning = true;
        currentProgress = 0.0;
        view.setProgress(0);
        timeline.play();
        view.showAndWait(); // Blocks until closed
    }

    private void updateGame() {
        if (isRunning && currentProgress < 1.0) {
            currentProgress += model.getFillSpeed();
            if (currentProgress > 1.0) currentProgress = 1.0;
            view.setProgress(currentProgress);
        }
    }

    private void handleStop() {
        if (isRunning) {
            stopLoop();
            calculateResult();
            view.close();
        }
    }

    private void stopLoop() {
        isRunning = false;
        timeline.stop();
    }

    private void calculateResult() {
        boolean won = currentProgress >= model.getTargetMin() && currentProgress <= model.getTargetMax();

        if (won) {
            result = new MinigameResult(true, 20, 
                "🎉 Perfect timing! " + pet.getName() + " is so happy! (+20 happiness)");
        } else {
            result = new MinigameResult(false, -5, 
                "😔 Missed the zone! " + pet.getName() + " is disappointed. (-5 happiness)");
        }
    }

    public MinigameResult getResult() {
        return result;
    }
}