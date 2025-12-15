package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.ui.MiniGameView;

import javax.swing.*;

/**
 * Controller responsible for wiring the minigame view with the game logic and the pet model.
 * Handles user input, validates guesses, and updates the view with minigame results.
 */
public class MiniGameController {
    private MiniGameView view;
    private GuessingGame game;
    private PetModel pet;

    /**
     * Creates a new {@code MiniGameController} for the given view and pet.
     *
     * @param view the minigame UI component
     * @param pet  the pet model whose state is affected by the minigame
     */
    public MiniGameController(MiniGameView view, PetModel pet) {
        this.view = view;
        this.pet = pet;
        this.game = new GuessingGame();

        initializeListeners();
    }

    /**
     * Registers all event listeners on the associated view.
     */
    private void initializeListeners() {
        view.getSubmitButton().addActionListener(e -> handleGuess());
        view.getPlayAgainButton().addActionListener(e -> startNewGame());
        view.getGuessField().addActionListener(e -> handleGuess());
    }

    /**
     * Handles a guess submitted by the user.
     * <p>
     * Validates the input, checks it against the current game, updates the pet,
     * and forwards the result to the view. Shows dialogs for invalid input.
     */
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

            MinigameResult result = game.checkGuess(guess);
            pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, result.happinessDelta());
            view.displayResult(result.won(), result.message());

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(view, "Please enter a valid number!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Starts a new round of the minigame by generating a new target number
     * and resetting the view state.
     */
    private void startNewGame() {
        game.generateNewNumber();
        view.resetGame();
    }

    /**
     * Returns the view associated with this controller.
     *
     * @return the {@link MiniGameView} managed by this controller
     */
    public MiniGameView getView() {
        return view;
    }
}