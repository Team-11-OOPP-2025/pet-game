package com.eleven.pet.daily_reward;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.daily_reward.Chest;
import com.eleven.pet.daily_reward.ChestComponent;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class DailyRewardView extends StackPane {
    private final PetModel model;
    private final PetController controller;
    
    private Label subTitle;
    private Label timerLabel;
    private HBox chestRow;
    private AnimationTimer timer;
    
    // Flag to track if we need to regenerate chests when the timer hits 0
    private boolean needsRefresh = false;

    public DailyRewardView(PetModel model, PetController controller) {
        this.model = model;
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        this.setVisible(false); // Hidden by default

        // Backdrop
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        backdrop.setOnMouseClicked(e -> toggle(false));

        // Main Panel
        VBox panel = new VBox(15);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(800, 450);
        panel.setStyle("-fx-background-color: #fdf5e6; -fx-background-radius: 20; -fx-border-color: #8b4513; -fx-border-width: 5; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        Label title = new Label("DAILY REWARDS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#8b4513"));

        subTitle = new Label("Select a chest to claim your prize!");
        subTitle.setFont(Font.font("Arial", 16));
        subTitle.setTextFill(Color.web("#555"));

        timerLabel = new Label();
        timerLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 20));
        timerLabel.setTextFill(Color.RED);
        timerLabel.setVisible(false);

        // Chest Row Container
        chestRow = new HBox(30);
        chestRow.setAlignment(Pos.CENTER);
        
        // Generate initial chests
        refreshChests();

        Button closeBtn = new Button("CLOSE");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeBtn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 10 30;");
        closeBtn.setOnAction(e -> toggle(false));

        panel.getChildren().addAll(title, subTitle, timerLabel, chestRow, closeBtn);
        this.getChildren().addAll(backdrop, panel);

        setupTimer();
    }
    
    /**
     * asks Controller for new chests and builds the UI components.
     */
    private void refreshChests() {
        chestRow.getChildren().clear();
        
        // Controller Logic: Get valid chest options
        List<Chest> chests = controller.generateDailyRewardOptions();
        
        for (Chest chestModel : chests) {
            ChestComponent visualChest = new ChestComponent(chestModel);
            visualChest.setScaleX(0.9);
            visualChest.setScaleY(0.9);

            visualChest.setOnOpen(() -> {
                // Controller Logic: Check availability
                if (controller.isDailyRewardAvailable()) {
                    
                    // Controller Logic: Claim logic
                    controller.claimDailyReward(chestModel);
                    
                    // View Logic: Set flags and update UI
                    needsRefresh = true; 
                    updateState(); 
                }
            });

            chestRow.getChildren().add(visualChest);
        }
    }

    private void setupTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                updateState();
            }
        };
    }

    public void toggle(boolean show) {
        if (show) {
            updateState();
            timer.start();
            this.setVisible(true);
            this.setOpacity(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(300), this);
            ft.setToValue(1.0);
            
            if (this.getChildren().size() > 1) {
                ScaleTransition st = new ScaleTransition(Duration.millis(300), this.getChildren().get(1)); 
                st.setFromX(0.8); st.setFromY(0.8);
                st.setToX(1.0); st.setToY(1.0);

                ParallelTransition pt = new ParallelTransition(ft, st);
                pt.play();
            } else {
                ft.play();
            }
        } else {
            timer.stop();
            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setToValue(0);
            ft.setOnFinished(e -> this.setVisible(false));
            ft.play();
        }
    }

    private void updateState() {
        // We still check the model for the actual value to drive the timer text
        double cooldown = model.getRewardCooldown();

        if (cooldown > 0) {
            timerLabel.setText("Next reward in: " + formatGameTime(cooldown));
            timerLabel.setVisible(true);
            subTitle.setText("Come back later for more!");
            
            chestRow.setDisable(true);
            chestRow.setOpacity(0.5);
        } else {
            if (needsRefresh) {
                refreshChests();
                needsRefresh = false;
            }
            
            timerLabel.setVisible(false);
            subTitle.setText("Select a chest to claim your prize!");
            
            chestRow.setDisable(false);
            chestRow.setOpacity(1.0);
        }
    }

    private String formatGameTime(double gameHours) {
        int hours = (int) gameHours;
        int minutes = (int) ((gameHours - hours) * 60);

        return String.format("%02dh %02dm", hours, minutes);
    }
}