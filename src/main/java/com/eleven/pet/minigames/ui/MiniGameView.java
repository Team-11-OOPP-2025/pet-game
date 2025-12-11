package com.eleven.pet.minigames.ui;

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

public class MiniGameView {

    private final Stage stage;
    private final ProgressBar progressBar;
    private final Button stopButton;

    public MiniGameView(String title, double targetMin, double targetMax) {
        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.setResizable(false);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2c3e50;");

        // Instructions
        Label instructions = new Label("Stop the bar between the markers!");
        instructions.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Progress bar container
        StackPane progressContainer = new StackPane();
        progressContainer.setPrefHeight(50);

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(40);
        progressBar.setStyle("-fx-accent: #3498db;");

        // Calculate visual marker positions
        // 400px width. Center is 0. 
        double width = 400;
        double startX = -width / 2;

        Line leftMarker = createMarker();
        leftMarker.setTranslateX(startX + (targetMin * width));

        Line rightMarker = createMarker();
        rightMarker.setTranslateX(startX + (targetMax * width));

        progressContainer.getChildren().addAll(progressBar, leftMarker, rightMarker);

        // Stop button
        stopButton = new Button("STOP!");
        stopButton.setPrefSize(200, 50);
        stopButton.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #e74c3c; -fx-text-fill: white;");

        root.getChildren().addAll(instructions, progressContainer, stopButton);

        Scene scene = new Scene(root, 500, 250);
        stage.setScene(scene);
    }

    private Line createMarker() {
        Line line = new Line();
        line.setStartY(0);
        line.setEndY(50);
        line.setStroke(Color.LIME);
        line.setStrokeWidth(4);
        return line;
    }

    public void showAndWait() {
        stage.showAndWait();
    }

    public void close() {
        stage.close();
    }

    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }

    public Button getStopButton() {
        return stopButton;
    }

    public Stage getStage() {
        return stage;
    }
}