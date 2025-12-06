package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TimingGame implements Minigame {
    private static final double TARGET_MIN = 0.40; // 40%
    private static final double TARGET_MAX = 0.60; // 60%
    private static final double FILL_SPEED = 0.01; // Progress per frame (1% per 100ms)
    
    @Override
    public String getName() {
        return "Timing Challenge";
    }
    
    @Override
    public MinigameResult play(PetModel pet) {
        // Create a holder for the result
        MinigameResult[] resultHolder = new MinigameResult[1];
        
        // Show dialog on JavaFX thread and wait
        if (Platform.isFxApplicationThread()) {
            resultHolder[0] = showMinigameDialogSync(pet);
        } else {
            Platform.runLater(() -> {
                resultHolder[0] = showMinigameDialogSync(pet);
            });
        }
        
        return resultHolder[0];
    }
    
    private MinigameResult showMinigameDialogSync(PetModel pet) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(getName());
        dialog.setResizable(false);
        
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");
        
        // Instructions
        Label instructions = new Label("Stop the bar between the markers!");
        instructions.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Progress bar container with markers
        StackPane progressContainer = new StackPane();
        progressContainer.setPrefHeight(50);
        
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(40);
        progressBar.setStyle("-fx-accent: #3498db;");
        
        // Target zone markers
        Line leftMarker = new Line();
        leftMarker.setStartY(0);
        leftMarker.setEndY(50);
        leftMarker.setStroke(Color.LIME);
        leftMarker.setStrokeWidth(4);
        leftMarker.setTranslateX(-400/2 + (TARGET_MIN * 400)); // Position at 40%
        
        Line rightMarker = new Line();
        rightMarker.setStartY(0);
        rightMarker.setEndY(50);
        rightMarker.setStroke(Color.LIME);
        rightMarker.setStrokeWidth(4);
        rightMarker.setTranslateX(-400/2 + (TARGET_MAX * 400)); // Position at 60%
        
        progressContainer.getChildren().addAll(progressBar, leftMarker, rightMarker);
        
        // Stop button
        Button stopButton = new Button("STOP!");
        stopButton.setPrefSize(200, 50);
        stopButton.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        root.getChildren().addAll(instructions, progressContainer, stopButton);
        
        // Result holder
        MinigameResult[] result = new MinigameResult[1];
        double[] currentProgress = {0.0};
        boolean[] stopped = {false};
        
        // Timeline for filling the bar
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), _ -> {
            if (!stopped[0] && currentProgress[0] < 1.0) {
                currentProgress[0] += FILL_SPEED;
                if (currentProgress[0] > 1.0) currentProgress[0] = 1.0;
                progressBar.setProgress(currentProgress[0]);
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Stop button action
        stopButton.setOnAction(_ -> {
            if (!stopped[0]) {
                stopped[0] = true;
                timeline.stop();
                
                // Check if in target zone
                boolean won = currentProgress[0] >= TARGET_MIN && currentProgress[0] <= TARGET_MAX;
                
                if (won) {
                    result[0] = new MinigameResult(true, 20, 
                        "🎉 Perfect timing! " + pet.getName() + " is so happy! (+20 happiness)");
                } else {
                    result[0] = new MinigameResult(false, -5, 
                        "😔 Missed the zone! " + pet.getName() + " is disappointed. (-5 happiness)");
                }
                
                dialog.close();
            }
        });
        
        // Handle window close
        dialog.setOnCloseRequest(_ -> {
            timeline.stop();
            if (result[0] == null) {
                result[0] = new MinigameResult(false, -5, 
                    pet.getName() + " gave up on the game. (-5 happiness)");
            }
        });
        
        Scene scene = new Scene(root, 500, 250);
        dialog.setScene(scene);
        dialog.showAndWait(); // This blocks until dialog is closed
        
        return result[0];
    }
}
