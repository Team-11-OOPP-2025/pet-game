
package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.ui.MiniGameView;

import javax.swing.*;

public class MiniGameController {
    private MiniGameView view;
    private GuessingGame game;
    private PetModel pet;
    
    public MiniGameController(MiniGameView view, PetModel pet) {
        this.view = view;
        this.pet = pet;
        this.game = new GuessingGame();
        
        initializeListeners();
    }
    
    private void initializeListeners() {
        view.getSubmitButton().addActionListener(e -> handleGuess());
        view.getPlayAgainButton().addActionListener(e -> startNewGame());
        view.getGuessField().addActionListener(e -> handleGuess());
    }
    
    private void handleGuess() {
        String input = view.getGuessField().getText().trim();
        
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Please enter a number!", "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            int guess = Integer.parseInt(input);
            
            if (guess < game.getMinNumber() || guess > game.getMaxNumber()) {
                JOptionPane.showMessageDialog(view, 
                    String.format("Please enter a number between %d and %d!", game.getMinNumber(), game.getMaxNumber()), 
                    "Invalid Range", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            MinigameResult result = game.checkGuess(guess, pet);
            view.displayResult(result.won(), result.message());
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Please enter a valid number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void startNewGame() {
        game.generateNewNumber();
        view.resetGame();
    }
    
    public MiniGameView getView() {
        return view;
    }
}