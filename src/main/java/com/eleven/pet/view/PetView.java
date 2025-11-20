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
    private StackPane miniGamesButtonContainer;
    private Text foodCounterText;
    private Rectangle hungerFillRect;
    private Rectangle energyFillRect;
    private Rectangle cleanFillRect;
    private Rectangle happinessFillRect;
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
        
        StackPane happinessBar = createHappinessBar();
        StackPane.setAlignment(happinessBar, Pos.TOP_LEFT);
        StackPane.setMargin(happinessBar, new Insets(90, 20, 20, 20));
        root.getChildren().add(happinessBar);
        
        StackPane hungerBar = createHungerBar();
        StackPane.setAlignment(hungerBar, Pos.TOP_LEFT);
        StackPane.setMargin(hungerBar, new Insets(148, 20, 20, 20));
        root.getChildren().add(hungerBar);

        StackPane energyBar = createEnergyBar();
        StackPane.setAlignment(energyBar, Pos.TOP_LEFT);
        StackPane.setMargin(energyBar, new Insets(193, 20, 20, 20));
        root.getChildren().add(energyBar);
        
        StackPane cleanBar = createCleanBar();
        StackPane.setAlignment(cleanBar, Pos.TOP_LEFT);
        StackPane.setMargin(cleanBar, new Insets(238, 20, 20, 20));
        root.getChildren().add(cleanBar);
        
        feedButtonContainer = createFeedButton();
        StackPane.setAlignment(feedButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(feedButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(feedButtonContainer);
        
        miniGamesButtonContainer = createMiniGamesButton();
        StackPane.setAlignment(miniGamesButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(miniGamesButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(miniGamesButtonContainer);
        
        HBox foodCounter = createFoodCounter();
        StackPane.setAlignment(foodCounter, Pos.TOP_RIGHT);
        StackPane.setMargin(foodCounter, new Insets(90, 20, 0, 0));
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
        
        Button button = new Button("FEED");
        button.setPrefSize(120, 50);
        button.setMaxSize(120, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        container.getChildren().addAll(bgRect, button);
        
        button.setOnAction(e -> {
            if (controller != null) {
                controller.handleFeed();
            }
            updateHungerBarOnFeed();
        });
        
        return container;
    }
    
    private StackPane createMiniGamesButton() {
        StackPane container = new StackPane();
        container.setPrefSize(140, 50);
        container.setMaxSize(140, 50);
        container.setMinSize(140, 50);
        
        Rectangle bgRect = new Rectangle(140, 50);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        Button button = new Button("PLAY");
        button.setPrefSize(140, 50);
        button.setMaxSize(140, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        container.getChildren().addAll(bgRect, button);
        
        // Button does nothing for now
        button.setOnAction(e -> {
            // Placeholder for future mini games functionality
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
    
    private void updateBackground(boolean isDaytime) {
        if (backgroundView != null) {
            Image newBackground = isDaytime ? dayBackground : nightBackground;
            if (newBackground != null && !newBackground.isError()) {
                backgroundView.setImage(newBackground);
            }
        }
    }
    
    private StackPane createHappinessBar() {
        StackPane container = new StackPane();
        container.setPrefSize(225, 38);
        container.setMaxSize(225, 38);
        container.setMinSize(225, 38);
        
        Rectangle bgRect = new Rectangle(225, 38);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        happinessFillRect = new Rectangle(225, 38);
        happinessFillRect.setFill(Color.web("#f4d03f"));
        StackPane.setAlignment(happinessFillRect, Pos.CENTER_LEFT);
        
        Text symbol = new Text("😃");
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        StackPane.setAlignment(symbol, Pos.CENTER_LEFT);
        StackPane.setMargin(symbol, new Insets(0, 0, 0, 10));
        
        Text label = new Text("Happiness");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        label.setFill(Color.BLACK);
        StackPane.setMargin(label, new Insets(0, 0, 0, 35));
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        
        container.getChildren().addAll(bgRect, happinessFillRect, symbol, label);
        
        return container;
    }

    private StackPane createHungerBar() {
        StackPane container = new StackPane();
        container.setPrefSize(150, 25);
        container.setMaxSize(150, 25);
        container.setMinSize(150, 25);
        
        Rectangle bgRect = new Rectangle(150, 25);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        hungerFillRect = new Rectangle(0, 25);
        hungerFillRect.setFill(Color.web("#2ecc71"));
        StackPane.setAlignment(hungerFillRect, Pos.CENTER_LEFT);
     
        Text symbol = new Text("🍖 ");
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        StackPane.setAlignment(symbol, Pos.CENTER_LEFT);
        StackPane.setMargin(symbol, new Insets(0, 0, 0, 5));
        
        Text label = new Text("Hunger");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.BLACK);
        StackPane.setMargin(label, new Insets(0, 0, 0, 25));
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        
        container.getChildren().addAll(bgRect, hungerFillRect, symbol, label);
        
        return container;
    }

    private StackPane createEnergyBar() {
        StackPane container = new StackPane();
        container.setPrefSize(150, 25);
        container.setMaxSize(150, 25);
        container.setMinSize(150, 25);
        
        Rectangle bgRect = new Rectangle(150, 25);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        energyFillRect = new Rectangle(150, 25);
        energyFillRect.setFill(Color.web("#f39c12"));
        StackPane.setAlignment(energyFillRect, Pos.CENTER_LEFT);

        Text symbol = new Text("⚡️ ");
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        StackPane.setAlignment(symbol, Pos.CENTER_LEFT);
        StackPane.setMargin(symbol, new Insets(0, 0, 0, 5));
        
        Text label = new Text("Energy");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.BLACK);
        StackPane.setMargin(label, new Insets(0, 0, 0, 25));
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        
        container.getChildren().addAll(bgRect, energyFillRect, symbol, label);
        
        return container;
    }

    private StackPane createCleanBar() {
        StackPane container = new StackPane();
        container.setPrefSize(150, 25);
        container.setMaxSize(150, 25);
        container.setMinSize(150, 25);
        
        Rectangle bgRect = new Rectangle(150, 25);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);
        
        cleanFillRect = new Rectangle(150, 25);
        cleanFillRect.setFill(Color.web("#3498db"));
        StackPane.setAlignment(cleanFillRect, Pos.CENTER_LEFT);
        
        Text symbol = new Text("🧽 ");
        symbol.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        StackPane.setAlignment(symbol, Pos.CENTER_LEFT);
        StackPane.setMargin(symbol, new Insets(0, 0, 0, 5));
        
        Text label = new Text("Clean");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.BLACK);
        StackPane.setMargin(label, new Insets(0, 0, 0, 25));
        StackPane.setAlignment(label, Pos.CENTER_LEFT);
        
        container.getChildren().addAll(bgRect, cleanFillRect, symbol, label);
        
        return container;
    }
    
    private void updateHungerBarOnFeed() {
        if (hungerFillRect == null) return;
        
        double currentWidth = hungerFillRect.getWidth();
        double newWidth = Math.min(currentWidth + 20, 150);
        
        hungerFillRect.setWidth(newWidth);
    }
    
    private void updateHungerBar() {
        if (petModel == null || hungerFillRect == null) return;
        
        double hunger = petModel.getHunger();
        double percentage = hunger / 100.0;
        
        hungerFillRect.setWidth(150 * percentage);
    }
}
