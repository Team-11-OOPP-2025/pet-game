package com.eleven.pet.minigames.impl;

import com.eleven.pet.minigames.GameSession;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.ui.ViewConstants;
import com.google.auto.service.AutoService;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.function.Consumer;

@AutoService(Minigame.class)
public class TimingGame implements Minigame {

    /**
     * @return the name of the minigame
     */
    @Override
    public String getName() {
        return "Timing Game";
    }

    /**
     * @return a new instance of the timing game session
     */
    @Override
    public GameSession createSession() {
        return new Session();
    }

    private static class Session implements GameSession {

        private Consumer<MinigameResult> onFinish;

        public static final double TARGET_MIN = 0.40;
        public static final double TARGET_MAX = 0.60;
        public static final double FILL_SPEED = 0.02;

        private static final int WIN_HAPPINESS = 20;
        private static final int LOSE_HAPPINESS = -5;


        private VBox viewLayout;
        private ProgressBar timingBar;
        private Button stopBtn;
        private Label resultLabel;

        private Timeline timingLoop;
        private double progress = 0.0;
        private boolean isRunning = false;

        /**
         * Returns the view for this timing game session.
         *
         * @return the game view
         */
        @Override
        public Pane getView() {
            if (viewLayout == null) {
                createView();
                startGame();
            }
            return viewLayout;
        }

        /**
         * Starts the timing game session.
         *
         * @param onFinish callback to invoke when the game ends
         */
        @Override
        public void start(java.util.function.Consumer<com.eleven.pet.minigames.MinigameResult> onFinish) {
            this.onFinish = onFinish;
            startGame();
        }

        private void createView() {
            viewLayout = new VBox(20);
            viewLayout.setAlignment(Pos.CENTER);
            viewLayout.setStyle("-fx-background-color: #2c3e50; -fx-padding: 20; -fx-background-radius: 5;");

            Label title = new Label("STOP IN THE ZONE!");
            title.setTextFill(Color.WHITE);
            title.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 16));

            // Bar Container with Markers
            StackPane barContainer = new StackPane();
            barContainer.setMaxWidth(200);

            timingBar = new ProgressBar(0);
            timingBar.setPrefWidth(200);
            timingBar.setPrefHeight(30);
            timingBar.setStyle("-fx-accent: #3498db;");

            barContainer.getChildren().add(timingBar);
            addMarkers(barContainer);

            stopBtn = new Button("STOP!");
            stopBtn.getStyleClass().addAll(ViewConstants.PIXEL_BUTTON_STYLE_CLASS);
            stopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-family: '" + ViewConstants.FONT_FAMILY + "'; -fx-font-size: 14px; -fx-font-weight: bold;");
            stopBtn.setOnAction(_ -> stopGame());

            resultLabel = new Label();
            resultLabel.setFont(Font.font(ViewConstants.FONT_FAMILY, 14));
            resultLabel.setTextFill(Color.WHITE);

            viewLayout.getChildren().addAll(title, barContainer, stopBtn, resultLabel);
        }

        private void addMarkers(Pane container) {
            double width = 200;
            double startX = -width / 2.0;

            Line l1 = new Line(0, 0, 0, 35);
            l1.setStroke(Color.LIME);
            l1.setStrokeWidth(3);
            l1.setTranslateX(startX + (TARGET_MIN * width));

            Line l2 = new Line(0, 0, 0, 35);
            l2.setStroke(Color.LIME);
            l2.setStrokeWidth(3);
            l2.setTranslateX(startX + (TARGET_MAX * width));

            container.getChildren().addAll(l1, l2);
        }

        private void startGame() {
            progress = 0.0;
            isRunning = true;
            if (timingLoop != null) timingLoop.stop();

            timingLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> update()));
            timingLoop.setCycleCount(Timeline.INDEFINITE);
            timingLoop.play();
        }

        private void update() {
            if (!isRunning) return;
            progress += FILL_SPEED;
            if (progress >= 1.0) {
                progress = 1.0;
                stopGame(); // Auto-fail
            }
            timingBar.setProgress(progress);
        }

        private void stopGame() {
            if (!isRunning) return;
            isRunning = false;
            timingLoop.stop();
            stopBtn.setDisable(true);

            boolean won = progress >= TARGET_MIN && progress <= TARGET_MAX;

            // View Update
            String msg = won
                    ? String.format("PERFECT! (+%d Happiness)", WIN_HAPPINESS)
                    : String.format("Missed! (%d Happiness)", LOSE_HAPPINESS);
            resultLabel.setText(msg);
            resultLabel.setTextFill(won ? Color.LIME : Color.RED);

            if (onFinish != null) {
                // Delay slightly so the user sees "Correct!" before the window closes
                PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
                delay.setOnFinished(_ -> {
                    int happiness = won ? WIN_HAPPINESS : LOSE_HAPPINESS;
                    onFinish.accept(new MinigameResult(won, happiness, msg));
                });
                delay.play();
            }
        }
    }
}