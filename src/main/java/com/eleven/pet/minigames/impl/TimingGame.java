// src/main/java/com/eleven/pet/minigames/impl/TimingGame.java
package com.eleven.pet.minigames.impl;

import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.ui.ViewConstants;
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

public class TimingGame implements Minigame {

    public static final double TARGET_MIN = 0.40;
    public static final double TARGET_MAX = 0.60;
    public static final double FILL_SPEED = 0.02;
    
    private static final int WIN_HAPPINESS = 20;
    private static final int LOSE_HAPPINESS = -5;

    private PetModel pet;
    private Runnable onFinish;
    
    private VBox viewLayout;
    private ProgressBar timingBar;
    private Button stopBtn;
    private Label resultLabel;
    
    private Timeline timingLoop;
    private double progress = 0.0;
    private boolean isRunning = false;

    @Override
    public String getName() {
        return "Timing Challenge";
    }

    @Override
    public void initialize(PetModel pet, Runnable onFinish) {
        this.pet = pet;
        this.onFinish = onFinish;
    }

    @Override
    public Pane getView() {
        if (viewLayout == null) {
            createView();
            startGame();
        }
        return viewLayout;
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
        stopBtn.getStyleClass().addAll("pixel-btn");
        stopBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-family: '" + ViewConstants.FONT_FAMILY + "'; -fx-font-size: 14px; -fx-font-weight: bold;");
        stopBtn.setOnAction(e -> stopGame());

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
        
        // Logic Update
        int happinessDelta = won ? WIN_HAPPINESS : LOSE_HAPPINESS;
        if (pet != null) {
            pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, happinessDelta);
        }

        // View Update
        resultLabel.setText(won ? "PERFECT! (+Happiness)" : "Missed! (-Happiness)");
        resultLabel.setTextFill(won ? Color.LIME : Color.RED);

        // Finish after delay
        PauseTransition delay = new PauseTransition(Duration.seconds(1.5));
        delay.setOnFinished(e -> {
            if (onFinish != null) onFinish.run();
        });
        delay.play();
    }
}