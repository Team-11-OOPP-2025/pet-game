// src/main/java/com/eleven/pet/minigames/impl/GuessingGame.java
package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.ui.ViewConstants;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Random;

public class GuessingGame implements Minigame {

    private static final int MIN_NUMBER = 1;
    private static final int MAX_NUMBER = 5;
    private static final int WIN_HAPPINESS = 20;
    private static final int LOSE_HAPPINESS = -5;

    private int secretNumber;
    private final Random random;
    
    private PetModel pet;
    private Runnable onFinish;
    
    private VBox viewLayout;
    private TextField guessField;
    private Label resultLabel;
    private Button submitBtn;

    public GuessingGame() {
        this.random = new Random();
    }

    @Override
    public String getName() {
        return "Guessing Game";
    }

    @Override
    public void initialize(PetModel pet, Runnable onFinish) {
        this.pet = pet;
        this.onFinish = onFinish;
        generateNewNumber();
    }

    @Override
    public Pane getView() {
        if (viewLayout == null) {
            createView();
        }
        return viewLayout;
    }

    private void createView() {
        viewLayout = new VBox(15);
        viewLayout.setAlignment(Pos.CENTER);
        viewLayout.setStyle("-fx-background-color: #fdf5e6; -fx-padding: 20; -fx-background-radius: 5;");

        Label title = new Label("Guess (1-5)");
        title.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#8b4513"));

        guessField = new TextField();
        guessField.setPromptText("#");
        guessField.setMaxWidth(60);
        guessField.setAlignment(Pos.CENTER);
        guessField.setStyle("-fx-font-family: '" + ViewConstants.FONT_FAMILY + "'; -fx-font-size: 14px;");
        
        // Handle Enter key
        guessField.setOnAction(e -> processGuess());

        submitBtn = new Button("Submit");
        submitBtn.getStyleClass().addAll("pixel-btn", ViewConstants.PIXEL_BUTTON_PRIMARY);
        submitBtn.setOnAction(e -> processGuess());

        resultLabel = new Label();
        resultLabel.setFont(Font.font(ViewConstants.FONT_FAMILY, 12));
        resultLabel.setWrapText(true);
        resultLabel.setAlignment(Pos.CENTER);

        viewLayout.getChildren().addAll(title, guessField, submitBtn, resultLabel);
    }

    public void generateNewNumber() {
        this.secretNumber = random.nextInt(MAX_NUMBER - MIN_NUMBER + 1) + MIN_NUMBER;
    }
    
    private void processGuess() {
        try {
            String txt = guessField.getText();
            if (txt.isEmpty()) return;

            int val = Integer.parseInt(txt);
            boolean won = (val == secretNumber);
            
            // Logic Update
            int happinessDelta = won ? WIN_HAPPINESS : LOSE_HAPPINESS;
            if (pet != null) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, happinessDelta);
            }

            // View Update
            String msg = won 
                ? String.format("Correct! It was %d. (+%d Happy)", secretNumber, WIN_HAPPINESS) 
                : String.format("Wrong! It was %d. (%d Happy)", secretNumber, LOSE_HAPPINESS);
            
            resultLabel.setText(msg);
            resultLabel.setTextFill(won ? Color.GREEN : Color.RED);
            
            submitBtn.setDisable(true);
            guessField.setDisable(true);

            // Finish after delay
            PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
            delay.setOnFinished(e -> {
                if (onFinish != null) onFinish.run();
            });
            delay.play();

        } catch (NumberFormatException ex) {
            resultLabel.setText("Enter a number!");
        }
    }
    
    // Kept for testing compatibility
    public int getMinNumber() { return MIN_NUMBER; }
    public int getMaxNumber() { return MAX_NUMBER; }
    // Kept for testing compatibility, though logic is now internal
    public com.eleven.pet.minigames.MinigameResult checkGuess(int guess, PetModel p) {
        // Simple delegate for tests
        boolean won = (guess == secretNumber);
        return new com.eleven.pet.minigames.MinigameResult(won, won ? WIN_HAPPINESS : LOSE_HAPPINESS, "");
    }
}