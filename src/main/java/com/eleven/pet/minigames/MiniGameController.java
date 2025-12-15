package com.eleven.pet.minigames;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.impl.GuessingGame;
import com.eleven.pet.minigames.impl.TimingGame;
import com.eleven.pet.minigames.ui.MiniGameView;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.util.Random;

/**
 * Unified controller that manages the minigame logic for a {@link MiniGameView}.
 * <p>
 * It encapsulates both the {@link GuessingGame} and {@link TimingGame} logic,
 * randomly selects which game to start, forwards user input from the view to
 * the game logic, and applies the resulting {@link MinigameResult} to the
 * associated {@link PetModel}.
 * </p>
 */
public class MiniGameController {
    private final MiniGameView view;
    private final PetModel pet;
    private final Random random = new Random();
    private final Runnable onFinish;

    // Logic Modules
    private final GuessingGame guessingGameLogic;
    private final TimingGame timingGameLogic;

    // Timing Game State
    private Timeline timingLoop;
    private double timingProgress;
    private boolean isTimingRunning;

    /**
     * Creates a new {@code MiniGameController} that wires together the view, pet model
     * and minigame logic, sets up the listeners, and immediately starts a random game.
     *
     * @param view     the {@link MiniGameView} that displays and captures user interaction
     * @param pet      the {@link PetModel} whose state is updated based on game results
     * @param onFinish callback invoked when the game is finished (e.g. to close the view);
     *                 may be {@code null} if no action is required
     */
    public MiniGameController(MiniGameView view, PetModel pet, Runnable onFinish) {
        this.view = view;
        this.pet = pet;
        this.onFinish = onFinish;
        
        this.guessingGameLogic = new GuessingGame();
        this.timingGameLogic = new TimingGame();
        
        initView();
        initListeners();
        
        // AUTO-START RANDOM GAME
        startRandomGame();
    }

    /**
     * Initializes static parts of the view, such as timing markers
     * for the timing minigame.
     */
    private void initView() {
        view.setupTimingMarkers(TimingGame.TARGET_MIN, TimingGame.TARGET_MAX);
    }

    /**
     * Registers all event handlers required by the minigames on the view
     * (guess submission and timing stop button).
     */
    private void initListeners() {
        // Guessing Game
        view.getGuessSubmitBtn().setOnAction(e -> processGuess());
        view.getGuessField().setOnAction(e -> processGuess());

        // Timing Game
        view.getTimingStopBtn().setOnAction(e -> stopTimingGame());
    }
    
    /**
     * Starts one of the available minigames at random.
     * <p>
     * Currently, this randomly chooses between the guessing and timing games.
     * </p>
     */
    private void startRandomGame() {
        if (random.nextBoolean()) {
            startGuessingGame();
        } else {
            startTimingGame();
        }
    }

    /**
     * Finishes the current game after a short delay so the user
     * has time to see the result, then invokes the {@code onFinish} callback.
     */
    private void finishGame() {
        // Wait 1.5 seconds so user can see the result, then exit
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            if (onFinish != null) onFinish.run();
        });
        delay.play();
    }

    // --- Guessing Game Logic ---

    /**
     * Prepares and shows the guessing minigame.
     * <p>
     * Generates a new target number and switches the view to guessing mode.
     */
    private void startGuessingGame() {
        guessingGameLogic.generateNewNumber();
        view.showGuessing();
    }

    /**
     * Handles the user's guess input:
     * <ul>
     *   <li>Parses the guess from the input field</li>
     *   <li>Delegates validation to {@link GuessingGame}</li>
     *   <li>Updates the result label and pet model</li>
     *   <li>Disables further input and schedules game finish</li>
     * </ul>
     */
    private void processGuess() {
        try {
            String txt = view.getGuessField().getText();
            if (txt.isEmpty()) return;
            
            int val = Integer.parseInt(txt);
            MinigameResult res = guessingGameLogic.checkGuess(val, pet);
            
            view.getGuessResultLabel().setText(res.message());
            view.getGuessResultLabel().setTextFill(res.won() ? javafx.scene.paint.Color.GREEN : javafx.scene.paint.Color.RED);
            
            // Lock inputs and finish
            view.getGuessField().setDisable(true);
            view.getGuessSubmitBtn().setDisable(true);
            
            finishGame();

        } catch (NumberFormatException ex) {
            view.getGuessResultLabel().setText("Enter a number!");
        }
    }

    // --- Timing Game Logic ---

    /**
     * Starts the timing minigame:
     * <ul>
     *   <li>Resets timing progress</li>
     *   <li>Displays the timing UI</li>
     *   <li>Creates and starts an animation loop that fills the progress bar</li>
     * </ul>
     */
    private void startTimingGame() {
        view.showTiming();
        timingProgress = 0.0;
        isTimingRunning = true;

        if (timingLoop != null) timingLoop.stop();
        timingLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> updateTimingLoop()));
        timingLoop.setCycleCount(Timeline.INDEFINITE);
        timingLoop.play();
    }

    /**
     * Per-frame update for the timing minigame.
     * <p>
     * Increases the fill progress, updates the progress bar, and automatically
     * stops the game as a failure if the bar becomes full.
     * </p>
     */
    private void updateTimingLoop() {
        if (!isTimingRunning) return;

        timingProgress += TimingGame.FILL_SPEED;
        if (timingProgress >= 1.0) {
            timingProgress = 1.0;
            stopTimingGame(); // Fail auto-stop
        }
        view.getTimingBar().setProgress(timingProgress);
    }

    /**
     * Stops the timing minigame if it is currently running, evaluates the
     * result via {@link TimingGame}, updates the UI, and schedules game finish.
     */
    private void stopTimingGame() {
        if (!isTimingRunning) return;
        isTimingRunning = false;
        timingLoop.stop();

        MinigameResult res = timingGameLogic.checkResult(timingProgress, pet);
        
        view.getTimingResultLabel().setText(res.message());
        view.getTimingStopBtn().setDisable(true);
        
        finishGame();
    }
}