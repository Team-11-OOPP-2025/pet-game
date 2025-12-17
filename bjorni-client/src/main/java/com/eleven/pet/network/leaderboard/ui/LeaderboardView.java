package com.eleven.pet.network.leaderboard.ui;

import com.eleven.pet.network.leaderboard.LeaderboardService;
import com.eleven.pet.shared.LeaderboardEntry;
import com.eleven.pet.ui.ViewConstants;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;


/**
 * A popup view that displays global high scores fetched from the server.
 */
public class LeaderboardView extends StackPane {

    private final LeaderboardService client;
    private VBox scoreListContainer;
    private Label statusLabel;

    public LeaderboardView(LeaderboardService client) {
        this.client = client;
        initializeUI();
    }

    private void initializeUI() {
        this.setVisible(false);

        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        backdrop.setOnMouseClicked(_ -> toggle(false));

        // Main Panel Window
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.setMaxSize(500, 600);
        panel.setPadding(new Insets(30));
        panel.setStyle(ViewConstants.STYLE_CONTENT_PANEL);

        // eader
        Label title = new Label("LEADERBOARD");
        title.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#8b4513"));

        // Score List Area
        scoreListContainer = new VBox(10);
        scoreListContainer.setAlignment(Pos.TOP_CENTER);

        // ScrollPane for scrolling through many scores
        ScrollPane scrollPane = new ScrollPane(scoreListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: #fdf5e6; -fx-background-color: transparent; -fx-padding: 10;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Status/Loading Label
        statusLabel = new Label("Loading...");
        statusLabel.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.NORMAL, 16));
        statusLabel.setTextFill(Color.GRAY);

        // Close Button
        Button closeBtn = new Button("CLOSE");
        closeBtn.getStyleClass().addAll(ViewConstants.PIXEL_BUTTON_STYLE_CLASS, ViewConstants.PIXEL_BUTTON_DANGER);
        closeBtn.setOnAction(_ -> toggle(false));

        panel.getChildren().addAll(title, statusLabel, scrollPane, closeBtn);

        this.getChildren().addAll(backdrop, panel);
    }

    /**
     * Toggles the visibility of the leaderboard.
     * When opening, it automatically triggers a data refresh.
     */
    public void toggle(boolean show) {
        if (show) {
            this.setVisible(true);
            this.setOpacity(0);

            // Animation
            FadeTransition ft = new FadeTransition(Duration.millis(300), this);
            ft.setToValue(1.0);

            // Pop-in effect for the panel
            if (this.getChildren().size() > 1) {
                ScaleTransition st = new ScaleTransition(Duration.millis(300), this.getChildren().get(1));
                st.setFromX(0.8);
                st.setFromY(0.8);
                st.setToX(1.0);
                st.setToY(1.0);
                new ParallelTransition(ft, st).play();
            } else {
                ft.play();
            }

            refreshScores();
        } else {
            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setToValue(0);
            ft.setOnFinished(_ -> this.setVisible(false));
            ft.play();
        }
    }

    /**
     * Fetches scores from the client and updates the UI.
     */
    private void refreshScores() {
        scoreListContainer.getChildren().clear();
        statusLabel.setText("Fetching scores...");
        statusLabel.setVisible(true);

        client.getTopScores(10).thenAccept(scores -> {
            // UI updates must happen on JavaFX Application Thread
            Platform.runLater(() -> {
                statusLabel.setVisible(false);
                if (scores.isEmpty()) {
                    statusLabel.setText("No scores available yet.");
                    statusLabel.setVisible(true);
                    return;
                }

                int rank = 1;
                for (LeaderboardEntry entry : scores) {
                    scoreListContainer.getChildren().add(createScoreRow(rank++, entry));
                }
            });
        });
    }

    /**
     * Creates a single styled row for a score entry.
     */
    private HBox createScoreRow(int rank, LeaderboardEntry entry) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #fff8dc; -fx-background-radius: 10; -fx-border-color: #deb887; -fx-border-width: 2;");

        // Rank Circle
        Label rankLbl = new Label("#" + rank);
        rankLbl.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 18));
        rankLbl.setTextFill(Color.web("#8b4513"));
        rankLbl.setMinWidth(40);

        // Player Name & Game
        VBox infoBox = new VBox(2);
        Label nameLbl = new Label(entry.getPlayerName());
        nameLbl.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 16));

        Label gameLbl = new Label(entry.getGameName());
        gameLbl.setFont(Font.font(ViewConstants.FONT_FAMILY, 12));
        gameLbl.setTextFill(Color.GRAY);

        infoBox.getChildren().addAll(nameLbl, gameLbl);
        HBox.setHgrow(infoBox, Priority.ALWAYS); // Push score to the right

        // Score
        Label scoreLbl = new Label(String.valueOf(entry.getScore()));
        scoreLbl.setFont(Font.font(ViewConstants.FONT_FAMILY, FontWeight.BOLD, 20));
        scoreLbl.setTextFill(Color.web("#228b22")); // Forest Green

        row.getChildren().addAll(rankLbl, infoBox, scoreLbl);
        return row;
    }
}
