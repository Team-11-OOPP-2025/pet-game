package com.eleven.pet.minigames.ui;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.MiniGameController;

import javax.swing.*;
import java.awt.*;

/**
 * View component for the mini guessing game.
 * <p>
 * This panel renders the UI for a simple number guessing game,
 * including the title, instructions, input field, action buttons
 * and result/feedback labels. It is intended to be used together
 * with {@link MiniGameController} as part of a mini-game flow.
 * </p>
 */
public class MiniGameView extends JPanel {
    
    private JLabel titleLabel;
    private JLabel instructionLabel;
    private JTextField guessField;
    private JButton submitButton;
    private JButton playAgainButton;
    private JLabel resultLabel;
    private JLabel feedbackLabel;
    
    /**
     * Creates a new {@code MiniGameView} and initializes all Swing components.
     * The layout is split into a title/instruction area, an input area,
     * and a result/feedback area.
     */
    public MiniGameView() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Top panel - Title and instructions
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        titleLabel = new JLabel("🎮 Guessing Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        instructionLabel = new JLabel("Guess a number between 1 and 5!", SwingConstants.CENTER);
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        topPanel.add(titleLabel);
        topPanel.add(instructionLabel);
        
        // Center panel - Input area
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        guessField = new JTextField(5);
        guessField.setFont(new Font("Arial", Font.PLAIN, 18));
        submitButton = new JButton("Submit Guess");
        submitButton.setFont(new Font("Arial", Font.PLAIN, 14));
        centerPanel.add(new JLabel("Your guess:"));
        centerPanel.add(guessField);
        centerPanel.add(submitButton);
        
        // Bottom panel - Results and feedback
        JPanel bottomPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        resultLabel = new JLabel("", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 18));
        feedbackLabel = new JLabel("", SwingConstants.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        playAgainButton = new JButton("Play Again");
        playAgainButton.setFont(new Font("Arial", Font.PLAIN, 14));
        playAgainButton.setVisible(false);
        bottomPanel.add(resultLabel);
        bottomPanel.add(feedbackLabel);
        bottomPanel.add(playAgainButton);
        
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Updates the UI to display the result of a guess.
     *
     * @param won     {@code true} if the player guessed correctly, {@code false} otherwise
     * @param message additional feedback message to show under the result label
     */
    public void displayResult(boolean won, String message) {
        resultLabel.setText(won ? "🎉 You Won!" : "❌ Wrong Guess");
        resultLabel.setForeground(won ? new Color(34, 139, 34) : new Color(220, 20, 60));
        feedbackLabel.setText(message);
        playAgainButton.setVisible(true);
        submitButton.setEnabled(false);
        guessField.setEnabled(false);
    }
    
    /**
     * Resets the UI state so the player can start a new round.
     * <p>
     * This clears the input field, hides result/feedback messages,
     * re-enables user input, and focuses the guess field.
     * </p>
     */
    public void resetGame() {
        guessField.setText("");
        guessField.setEnabled(true);
        resultLabel.setText("");
        feedbackLabel.setText("");
        playAgainButton.setVisible(false);
        submitButton.setEnabled(true);
        guessField.requestFocus();
    }
    
    /**
     * Returns the text field used for entering guesses.
     * <p>
     * Primarily useful for controllers that need to add listeners
     * or read the current guess.
     * </p>
     *
     * @return the {@link JTextField} used for user guesses
     */
    public JTextField getGuessField() {
        return guessField;
    }
    
    /**
     * Returns the button used to submit a guess.
     *
     * @return the "Submit Guess" {@link JButton}
     */
    public JButton getSubmitButton() {
        return submitButton;
    }
    
    /**
     * Returns the button used to start another round after a guess.
     *
     * @return the "Play Again" {@link JButton}
     */
    public JButton getPlayAgainButton() {
        return playAgainButton;
    }
    
    /**
     * Creates and displays the mini-game in a new window.
     * <p>
     * This method is a convenience factory that instantiates the view
     * and its corresponding {@link MiniGameController}, attaches them
     * to a {@link JFrame}, and shows the game on the Event Dispatch Thread.
     * </p>
     *
     * @param model the {@link PetModel} used by the mini-game to interact
     *              with the current pet state and apply game effects
     */
    public static void showMiniGame(PetModel model) {
        SwingUtilities.invokeLater(() -> {
            JFrame gameFrame = new JFrame("Guessing Game");
            MiniGameView miniGameView = new MiniGameView();
            MiniGameController miniGameController = new MiniGameController(miniGameView, model);
            
            gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            gameFrame.add(miniGameView);
            gameFrame.setSize(500, 400);
            gameFrame.setLocationRelativeTo(null);
            gameFrame.setVisible(true);
        });
    }
}
