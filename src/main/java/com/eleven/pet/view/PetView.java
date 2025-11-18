package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class PetView {
    private PetModel petModel;
    private PetController controller;
    private ImageView backgroundView;
    private Image dayBackground;
    private Image nightBackground;
    
    public PetView(PetModel petModel, PetController controller) {
        this.petModel = petModel;
        this.controller = controller;
        loadBackgroundImages();
    }
    

    private void loadBackgroundImages() {
        try {
            System.out.println("=== Loading background images ===");
            
            var dayStream = getClass().getResourceAsStream("/images/living-room-background-day.png");
            if (dayStream != null) {
                dayBackground = new Image(dayStream);
                if (!dayBackground.isError()) {
                    System.out.println("✓ Day background loaded: " + dayBackground.getWidth() + "x" + dayBackground.getHeight());
                } else {
                    System.err.println("✗ Day background failed to load!");
                }
            } else {
                System.err.println("✗ Day background not found at /images/living-room-background-day.png");
            }
            
            var nightStream = getClass().getResourceAsStream("/images/living-room-background-night.png");
            if (nightStream != null) {
                nightBackground = new Image(nightStream);
                if (!nightBackground.isError()) {
                    System.out.println("✓ Night background loaded: " + nightBackground.getWidth() + "x" + nightBackground.getHeight());
                } else {
                    System.err.println("✗ Night background failed to load!");
                }
            } else {
                System.err.println("✗ Night background not found at /images/living-room-background-night.png");
                if (dayBackground != null) {
                    nightBackground = dayBackground;
                    System.out.println("⚠ Using day background as night background fallback");
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERROR loading backgrounds: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;"); // Dark fallback color
        
        try {
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
            
        } catch (Exception e) {
            System.err.println("ERROR initializing UI: " + e.getMessage());
            e.printStackTrace();
        }
        
        return root;
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
