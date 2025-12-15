package com.eleven.pet.minigames.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.eleven.pet.ui.ViewConstants.FONT_FAMILY;

import com.eleven.pet.ui.ViewConstants;

/**
 * A unified view that acts as the "TV Screen" for mini‑games.
 * <p>
 * This view can display the UI for:
 * <ul>
 *   <li>a simple number guessing game</li>
 *   <li>a timing/precision game using a progress bar</li>
 * </ul>
 * The controller is expected to bind game logic to the exposed controls
 * via the provided getters.
 * </p>
 */
public class MiniGameView extends StackPane {

    // --- Guessing Game UI ---
    private VBox guessingLayer;
    private TextField guessField;
    private Button guessSubmitBtn;
    private Label guessResultLabel;

    // --- Timing Game UI ---
    private VBox timingLayer;
    private ProgressBar timingBar;
    private StackPane timingBarContainer;
    private Button timingStopBtn;
    private Label timingResultLabel;

    /**
     * Creates a new {@code MiniGameView} with both the guessing and timing
     * game layers initialized but hidden. Use {@link #showGuessing()} or
     * {@link #showTiming()} to make a specific game visible.
     */
    public MiniGameView() {
        // TV Screen styling
        setStyle("-fx-background-color: #222; -fx-background-radius: 4;");
        setPadding(new Insets(10));

        initGuessingLayer();
        initTimingLayer();
    }

    // --- Initialization Helpers ---

    /**
     * Initializes the UI components for the guessing mini‑game.
     * <p>
     * Creates the layout, input field, submit button, and result label,
     * and adds the guessing layer to this {@link StackPane}.
     * </p>
     */
    private void initGuessingLayer() {
        guessingLayer = new VBox(10);
        guessingLayer.setAlignment(Pos.CENTER);
        guessingLayer.setStyle("-fx-background-color: #fdf5e6; -fx-padding: 20; -fx-background-radius: 5;");
        guessingLayer.setVisible(false);

        Label title = new Label("Guessing Game (1-5)");
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        title.setTextFill(Color.web("#8b4513"));

        guessField = new TextField();
        guessField.setPromptText("#");
        guessField.setMaxWidth(60);
        guessField.setAlignment(Pos.CENTER);
        guessField.setStyle("-fx-font-family: '" + FONT_FAMILY + "'; -fx-font-size: 14px;");

        guessSubmitBtn = createButton("Submit");
        
        guessResultLabel = new Label();
        guessResultLabel.setFont(Font.font(FONT_FAMILY, 12));
        guessResultLabel.setWrapText(true);
        guessResultLabel.setAlignment(Pos.CENTER);

        guessingLayer.getChildren().addAll(title, guessField, guessSubmitBtn, guessResultLabel);
        getChildren().add(guessingLayer);
    }

    /**
     * Initializes the UI components for the timing mini‑game.
     * <p>
     * Creates the progress bar, its container, stop button, and result label,
     * and adds the timing layer to this {@link StackPane}.
     * </p>
     */
    private void initTimingLayer() {
        timingLayer = new VBox(20);
        timingLayer.setAlignment(Pos.CENTER);
        timingLayer.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20; -fx-background-radius: 5;");
        timingLayer.setVisible(false);

        Label title = new Label("STOP IN THE ZONE!");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));

        // Bar Container
        timingBarContainer = new StackPane();
        timingBarContainer.setMaxWidth(200);
        
        timingBar = new ProgressBar(0);
        timingBar.setPrefWidth(200);
        timingBar.setPrefHeight(30);
        timingBar.setStyle("-fx-accent: #3498db;");
        
        timingBarContainer.getChildren().add(timingBar);

        timingStopBtn = createButton("STOP!");
        timingStopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-family: '"+FONT_FAMILY+"'; -fx-font-size: 14px; -fx-font-weight: bold;");

        timingResultLabel = new Label();
        timingResultLabel.setFont(Font.font(FONT_FAMILY, 14));
        timingResultLabel.setTextFill(Color.WHITE);

        timingLayer.getChildren().addAll(title, timingBarContainer, timingStopBtn, timingResultLabel);
        getChildren().add(timingLayer);
    }

    // --- Switching Methods ---

    /**
     * Shows the guessing game layer and hides the timing game layer.
     * <p>
     * Also resets the guessing game UI (clears input, enables controls,
     * clears the result text) and focuses the input field.
     * </p>
     */
    public void showGuessing() {
        guessingLayer.setVisible(true);
        timingLayer.setVisible(false);
        
        // Reset UI elements
        guessField.setText("");
        guessField.setDisable(false);
        guessSubmitBtn.setDisable(false);
        guessResultLabel.setText("");
        
        guessField.requestFocus();
    }

    /**
     * Shows the timing game layer and hides the guessing game layer.
     * <p>
     * Also resets the timing bar progress, enables the stop button,
     * and clears the result label.
     * </p>
     */
    public void showTiming() {
        guessingLayer.setVisible(false);
        timingLayer.setVisible(true);

        // Reset UI elements
        timingBar.setProgress(0);
        timingStopBtn.setDisable(false);
        timingResultLabel.setText("");
    }
    
    /**
     * Draws visual markers on the timing bar to indicate the target zone.
     *
     * @param minPct the start of the valid zone as a fraction in {@code [0.0, 1.0]}
     * @param maxPct the end of the valid zone as a fraction in {@code [0.0, 1.0]}
     *               (must be greater than or equal to {@code minPct})
     */
    public void setupTimingMarkers(double minPct, double maxPct) {
        timingBarContainer.getChildren().removeIf(node -> node instanceof Line); // Clear old markers
        
        double width = 200; // Fixed width defined above
        double startX = -width / 2.0;
        
        Line l1 = new Line(0, 0, 0, 35); 
        l1.setStroke(Color.LIME); 
        l1.setStrokeWidth(3);
        l1.setTranslateX(startX + (minPct * width));
        
        Line l2 = new Line(0, 0, 0, 35); 
        l2.setStroke(Color.LIME); 
        l2.setStrokeWidth(3);
        l2.setTranslateX(startX + (maxPct * width));
        
        timingBarContainer.getChildren().addAll(l1, l2);
    }

    /**
     * Creates a styled button with the shared "pixel" look and primary color.
     *
     * @param text the button label
     * @return a new {@link Button} instance with shared styling applied
     */
    private Button createButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll("pixel-btn", ViewConstants.PIXEL_BUTTON_PRIMARY);
        return btn;
    }

    // --- Getters ---

    /**
     * @return the text field used for entering the guess in the guessing game
     */
    public TextField getGuessField() { return guessField; }

    /**
     * @return the submit button for the guessing game
     */
    public Button getGuessSubmitBtn() { return guessSubmitBtn; }

    /**
     * @return the label showing feedback/result for the guessing game
     */
    public Label getGuessResultLabel() { return guessResultLabel; }

    /**
     * @return the progress bar used in the timing game
     */
    public ProgressBar getTimingBar() { return timingBar; }

    /**
     * @return the stop button used to end the timing game
     */
    public Button getTimingStopBtn() { return timingStopBtn; }

    /**
     * @return the label showing feedback/result for the timing game
     */
    public Label getTimingResultLabel() { return timingResultLabel; }
}