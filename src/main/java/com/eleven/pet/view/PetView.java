package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PetView {
    private PetModel petModel;
    private PetController controller;
    private ImageView backgroundView;
    private Image dayBackground;
    private Image nightBackground;
    private StackPane feedButtonContainer;
    private Rectangle feedFillRect;
    private Text foodCounterText;
    private Rectangle hungerFillRect;
    private ImageView petImageView;
    private Image petImage1;
    private Image petImage2;
    private boolean showingFirstImage = true;
    
    public PetView(PetModel petModel, PetController controller) {
        this.petModel = petModel;
        this.controller = controller;
        loadBackgroundImages();
        loadPetImages();
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

    private void loadPetImages() {
        try {
            var image1Stream = getClass().getResourceAsStream("/images/Bear.png");
            if (image1Stream != null) {
                petImage1 = new Image(image1Stream);
            }
            
            var image2Stream = getClass().getResourceAsStream("/images/SleepyBear.png");
            if (image2Stream != null) {
                petImage2 = new Image(image2Stream);
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
        
        petImageView = createPetImage();
        StackPane.setAlignment(petImageView, Pos.BOTTOM_CENTER);
        StackPane.setMargin(petImageView, new Insets(0, 0, 70, 0));
        root.getChildren().add(petImageView);
        
        StackPane hungerBar = createHungerBar();
        StackPane.setAlignment(hungerBar, Pos.TOP_LEFT);
        StackPane.setMargin(hungerBar, new Insets(20));
        root.getChildren().add(hungerBar);
        
        feedButtonContainer = createFeedButton();
        StackPane.setAlignment(feedButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(feedButtonContainer, new Insets(20));
        root.getChildren().add(feedButtonContainer);
        
        HBox foodCounter = createFoodCounter();
        StackPane.setAlignment(foodCounter, Pos.TOP_RIGHT);
        StackPane.setMargin(foodCounter, new Insets(20));
        root.getChildren().add(foodCounter);
        
        return root;
    }
    
    private ImageView createPetImage() {
        ImageView imageView = new ImageView();
        imageView.setImage(petImage1);
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);
        
        imageView.setOnMouseClicked(e -> {
            showingFirstImage = !showingFirstImage;
            imageView.setImage(showingFirstImage ? petImage1 : petImage2);
        });
        
        imageView.setStyle("-fx-cursor: hand;");
        
        return imageView;
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
            updateHungerBarOnFeed();
        });
        
        return container;
    }
    
    private HBox createFoodCounter() {
        HBox container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); -fx-background-radius: 10; -fx-padding: 10;");
        container.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);
        
        Text label = new Text("Food: ");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        label.setFill(Color.BLACK);
        
        foodCounterText = new Text("0");
        foodCounterText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        foodCounterText.setFill(Color.web("#e74c3c"));
        
        container.getChildren().addAll(label, foodCounterText);
        
        if (petModel != null) {
            updateFoodCounter();
            petModel.getFoodCountProperty().addListener((obs, oldVal, newVal) -> {
                updateFoodCounter();
            });
        }
        
        return container;
    }
    
    private void updateFoodCounter() {
        if (petModel == null || foodCounterText == null) return;
        foodCounterText.setText(String.valueOf(petModel.getFoodCount()));
    }
    
    private void updateFeedButtonAppearance(Button button) {
        if (petModel == null || feedFillRect == null) return;
        
        int foodCount = petModel.getFoodCount();
        double percentage = foodCount / 100.0;
        
        feedFillRect.setWidth(120 * percentage);
        button.setText("FEED");
    }
    
    private void updateBackground(boolean isDaytime) {
        if (backgroundView != null) {
            Image newBackground = isDaytime ? dayBackground : nightBackground;
            if (newBackground != null && !newBackground.isError()) {
                backgroundView.setImage(newBackground);
            }
        }
    }
    
    private StackPane createHungerBar() {
        StackPane container = new StackPane();
        container.setPrefSize(200, 30);
        container.setMaxSize(200, 30);
        container.setMinSize(200, 30);
        
        Rectangle bgRect = new Rectangle(200, 30);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        hungerFillRect = new Rectangle(0, 30);
        hungerFillRect.setFill(Color.web("#2ecc71"));
        StackPane.setAlignment(hungerFillRect, Pos.CENTER_LEFT);
        
        Text label = new Text("Hunger");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.BLACK);
        
        container.getChildren().addAll(bgRect, hungerFillRect, label);
        
        return container;
    }
    
    private void updateHungerBarOnFeed() {
        if (hungerFillRect == null) return;
        
        double currentWidth = hungerFillRect.getWidth();
        double newWidth = Math.min(currentWidth + 20, 200);
        
        hungerFillRect.setWidth(newWidth);
    }
    
    private void updateHungerBar() {
        if (petModel == null || hungerFillRect == null) return;
        
        double hunger = petModel.getHunger();
        double percentage = hunger / 100.0;
        
        hungerFillRect.setWidth(200 * percentage);
    }
}
