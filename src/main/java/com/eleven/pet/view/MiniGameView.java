package com.eleven.pet.view;

import javax.swing.*;
import java.awt.*;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.controller.MiniGameController;

public class MiniGameView extends JPanel {
    
    private JLabel titleLabel;
    private JLabel instructionLabel;
    private JTextField guessField;
    private JButton submitButton;
    private JButton playAgainButton;
    private JLabel resultLabel;
    private JLabel feedbackLabel;
    
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
    
    public void displayResult(boolean won, String message) {
        resultLabel.setText(won ? "🎉 You Won!" : "❌ Wrong Guess");
        resultLabel.setForeground(won ? new Color(34, 139, 34) : new Color(220, 20, 60));
        feedbackLabel.setText(message);
        playAgainButton.setVisible(true);
        submitButton.setEnabled(false);
        guessField.setEnabled(false);
    }
    
    public void resetGame() {
        guessField.setText("");
        guessField.setEnabled(true);
        resultLabel.setText("");
        feedbackLabel.setText("");
        playAgainButton.setVisible(false);
        submitButton.setEnabled(true);
        guessField.requestFocus();
    }
    
    public JTextField getGuessField() {
        return guessField;
    }
    
    public JButton getSubmitButton() {
        return submitButton;
    }
    
    public JButton getPlayAgainButton() {
        return playAgainButton;
    }
    
    /**
     * Creates and displays the mini-game in a new window.
     * @param model The PetModel to interact with
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
