package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PetView {
    private PetModel petModel;
    private PetController controller;
    private ImageView backgroundView;
    private Image dayBackground;
    private Image nightBackground;
    private StackPane feedButtonContainer;
    private Rectangle feedFillRect;
    
    public PetView(PetModel petModel, PetController controller) {
        this.petModel = petModel;
        this.controller = controller;
        loadBackgroundImages();
    }
    

    private void loadBackgroundImages() {
        try {
            var dayStream = getClass().getResourceAsStream("/images/DayBackground.png");
            if (dayStream != null) {
                dayBackground = new Image(dayStream);
            }
            
            var nightStream = getClass().getResourceAsStream("/images/NightBackground.png");
            if (nightStream != null) {
                nightBackground = new Image(nightStream);
            } else if (dayBackground != null) {
                nightBackground = dayBackground;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");
        
        backgroundView = new ImageView();
        
        if (petModel != null && petModel.getGameClock() != null) {
            updateBackground(petModel.getGameClock().isDaytime());
            petModel.getGameClock().isDaytimeProperty().addListener((obs, oldVal, newVal) -> {
                updateBackground(newVal);
            });
        } else {
            backgroundView.setImage(dayBackground);
        }
        
        backgroundView.setPreserveRatio(true);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());
        
        root.getChildren().add(backgroundView);
        
        feedButtonContainer = createFeedButton();
        StackPane.setAlignment(feedButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(feedButtonContainer, new Insets(20));
        root.getChildren().add(feedButtonContainer);
        
        return root;
    }
    
    private StackPane createFeedButton() {
        StackPane container = new StackPane();
        container.setPrefSize(120, 50);
        container.setMaxSize(120, 50);
        container.setMinSize(120, 50);
        
        Rectangle bgRect = new Rectangle(120, 50);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        feedFillRect = new Rectangle(0, 50);
        feedFillRect.setFill(Color.RED);
        StackPane.setAlignment(feedFillRect, Pos.CENTER_LEFT);
        
        Button button = new Button("FEED");
        button.setPrefSize(120, 50);
        button.setMaxSize(120, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        container.getChildren().addAll(bgRect, feedFillRect, button);
        
        updateFeedButtonAppearance(button);
        
        if (petModel != null) {
            petModel.getFoodCountProperty().addListener((obs, oldVal, newVal) -> {
                updateFeedButtonAppearance(button);
            });
        }
        
        button.setOnAction(e -> {
            if (controller != null) {
                controller.handleFeed();
            }
        });
        
        return container;
    }
    
    private void updateFeedButtonAppearance(Button button) {
        if (petModel == null || feedFillRect == null) return;
        
        int foodCount = petModel.getFoodCount();
        double percentage = foodCount / 100.0;
        
        feedFillRect.setWidth(120 * percentage);
        button.setText("FEED (" + foodCount + ")");
    }
    
    private void updateBackground(boolean isDaytime) {
        if (backgroundView != null) {
            Image newBackground = isDaytime ? dayBackground : nightBackground;
            if (newBackground != null && !newBackground.isError()) {
                backgroundView.setImage(newBackground);
            }
        }
    }
}
