package com.eleven.pet.daily_reward;

import com.eleven.pet.character.PetModel;

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

public class DailyRewardView extends StackPane {
    private final PetModel model;
    
    public DailyRewardView(PetModel model) {
        this.model = model;
        initializeUI();
    }

    private void initializeUI() {
        this.setVisible(false); // Hidden by default

        // Backdrop
        Region backdrop = new Region();
        backdrop.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        backdrop.setOnMouseClicked(e -> toggle(false));

        // Main Panel
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setMaxSize(800, 400);
        panel.setStyle("-fx-background-color: #fdf5e6; -fx-background-radius: 20; -fx-border-color: #8b4513; -fx-border-width: 5; -fx-padding: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        Label title = new Label("DAILY REWARDS");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#8b4513"));

        Label subTitle = new Label("Select a chest to claim your prize!");
        subTitle.setFont(Font.font("Arial", 16));
        subTitle.setTextFill(Color.web("#555"));

        // Row of 5 Chests
        HBox chestRow = new HBox(30);
        chestRow.setAlignment(Pos.CENTER);

        for (int i = 0; i < 5; i++) {
            Chest chestModel = new Chest(); 
            ChestComponent visualChest = new ChestComponent(chestModel);
            visualChest.setScaleX(0.9);
            visualChest.setScaleY(0.9);

            visualChest.setOnOpen(() -> {
                chestModel.open(model);
            });

            chestRow.getChildren().add(visualChest);
        }

        Button closeBtn = new Button("CLOSE");
        closeBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        closeBtn.setStyle("-fx-background-color: #8b4513; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 10 30;");
        closeBtn.setOnAction(e -> toggle(false));

        panel.getChildren().addAll(title, subTitle, chestRow, closeBtn);
        this.getChildren().addAll(backdrop, panel);
    }

    public void toggle(boolean show) {
        if (show) {
            this.setVisible(true);
            this.setOpacity(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(300), this);
            ft.setToValue(1.0);
            
            // Animate panel (2nd child)
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
            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setToValue(0);
            ft.setOnFinished(e -> this.setVisible(false));
            ft.play();
        }
    }
}
