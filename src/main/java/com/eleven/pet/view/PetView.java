package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.clock.DayCycle;

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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import java.util.Random;

/**
 * Your beautiful UI - migrated to new architecture!
 * All your visual design preserved, now with proper bindings.
 */
public class PetView {
    private final PetModel petModel;
    private final PetController controller;
    private final GameClock clock;

    // Your existing UI fields - ALL PRESERVED!
    private ImageView backgroundView;
    private Image earlyMorningBackground;
    private Image lateMorningBackground;
    private Image dayBackground;
    private Image eveningBackground;
    private Image earlyNightBackground;
    private Image deepNightBackground;
    private StackPane feedButtonContainer;
    private StackPane playButtonContainer;  // Renamed from miniGamesButtonContainer
    private Text foodCounterText;
    private Rectangle hungerFillRect;
    private Rectangle energyFillRect;
    private Rectangle cleanFillRect;
    private Rectangle happinessFillRect;
    private ImageView petImageView;
    private Image petImage1;
    private Image petImage2;
    private Image petImage3;
    private Image cryingBear1;
    private Image cryingBear2;
    private Timeline petImageSwitcher;
    private Random random = new Random();
    private boolean isCrying = false;

    public PetView(PetModel petModel, PetController controller) {
        this.petModel = petModel;
        this.controller = controller;
        this.clock = petModel.getGameClock();
        loadBackgroundImages();
        loadPetImages();
    }


    // YOUR EXACT CODE - KEPT AS-IS!
    private void loadBackgroundImages() {
        AssetLoader loader = AssetLoader.getInstance();
        earlyMorningBackground = loader.getImage("Dawn");
        lateMorningBackground = loader.getImage("Morning");
        dayBackground = loader.getImage("Day");
        eveningBackground = loader.getImage("Evening");
        earlyNightBackground = loader.getImage("EarlyNight");
        deepNightBackground = loader.getImage("DeepNight");
        
        // Fallback if DeepNight doesn't exist
        if (deepNightBackground == null && dayBackground != null) {
            deepNightBackground = dayBackground;
        }
    }

    // YOUR EXACT CODE - KEPT AS-IS!
    private void loadPetImages() {
        AssetLoader loader = AssetLoader.getInstance();
        petImage1 = loader.getImage("LookingLeftBear");
        petImage2 = loader.getImage("LookingRightBear");
        petImage3 = loader.getImage("Bear");
        cryingBear1 = loader.getImage("CryingBear1");
        cryingBear2 = loader.getImage("CryingBear2");
    }

    // YOUR LAYOUT CODE - KEPT AS-IS!
    public Pane initializeUI() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        backgroundView = new ImageView();

        // Updated to use cleaner clock access
        if (clock != null) {
            updateBackgroundByTime();
            clock.cycleProperty().addListener((_, _, newVal) -> {
                updateBackgroundByTime();
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

        // YOUR EXACT STAT BARS!
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

        playButtonContainer = createPlayButton();  // Updated name
        StackPane.setAlignment(playButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(playButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(playButtonContainer);

        HBox foodCounter = createFoodCounter();
        StackPane.setAlignment(foodCounter, Pos.TOP_RIGHT);
        StackPane.setMargin(foodCounter, new Insets(90, 20, 0, 0));
        root.getChildren().add(foodCounter);

        // NEW: Bind stat bars to model
        bindStatBarsToModel();

        // Start random pet image switching
        startPetImageSwitching();

        return root;
    }

    // YOUR EXACT CODE - KEPT AS-IS!
    private ImageView createPetImage() {
        ImageView imageView = new ImageView();
        imageView.setImage(petImage1);
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        // Make pet clickable to toggle sleepy state
        imageView.setOnMouseClicked(_ -> toggleCryingState());
        imageView.setStyle("-fx-cursor: hand;");

        return imageView;
    }

    // YOUR CODE - KEPT, SLIGHTLY UPDATED ACTION
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

        button.setOnAction(_ -> {
            if (controller != null) {
                controller.handleFeed();
                // No need to manually update bar - binding handles it!
            }
        });

        return container;
    }

    // RENAMED from createMiniGamesButton, now calls handlePlay
    private StackPane createPlayButton() {
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

        // NOW FUNCTIONAL!
        button.setOnAction(_ -> {
            if (controller != null) {
                controller.handlePlay();
            }
        });

        return container;
    }

    // YOUR EXACT CODE - KEPT AS-IS!
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
            petModel.getFoodCountProperty().addListener((_, _, _) -> {
                updateFoodCounter();
            });
        }

        return container;
    }

    // YOUR EXACT CODE!
    private void updateFoodCounter() {
        if (petModel == null || foodCounterText == null) return;
        foodCounterText.setText(String.valueOf(petModel.getFoodCount()));
    }

    // NEW: Update background based on time of day using DayCycle
    private void updateBackgroundByTime() {
        if (backgroundView == null || clock == null) return;

        DayCycle cycle = clock.getCycle();
        Image newBackground;

        switch (cycle) {
            case DEEP_NIGHT:
                newBackground = deepNightBackground != null ? deepNightBackground : dayBackground;
                break;
            case DAWN:
                newBackground = earlyMorningBackground != null ? earlyMorningBackground : dayBackground;
                break;
            case MORNING:
                newBackground = lateMorningBackground != null ? lateMorningBackground : dayBackground;
                break;
            case DAY:
                newBackground = dayBackground;
                break;
            case EVENING:
                newBackground = eveningBackground != null ? eveningBackground : dayBackground;
                break;
            case EARLY_NIGHT:
                newBackground = earlyNightBackground != null ? earlyNightBackground : deepNightBackground;
                break;
            default:
                newBackground = dayBackground;
                break;
        }

        if (newBackground != null && !newBackground.isError()) {
            backgroundView.setImage(newBackground);
        }
    }

    // YOUR EXACT STAT BAR CODE - ALL PRESERVED!
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

    // NEW: Automatic stat bar binding!
    private void bindStatBarsToModel() {
        if (petModel == null || petModel.getStats() == null) return;

        PetStats stats = petModel.getStats();

        // Bind each bar to its stat (0-100 mapped to width)
        stats.getStat(PetStats.STAT_HUNGER).addListener((obs, oldVal, newVal) -> {
            double percentage = newVal.intValue() / 100.0;
            hungerFillRect.setWidth(150 * percentage);
        });

        stats.getStat(PetStats.STAT_HAPPINESS).addListener((obs, oldVal, newVal) -> {
            double percentage = newVal.intValue() / 100.0;
            happinessFillRect.setWidth(225 * percentage);
        });

        stats.getStat(PetStats.STAT_ENERGY).addListener((obs, oldVal, newVal) -> {
            double percentage = newVal.intValue() / 100.0;
            energyFillRect.setWidth(150 * percentage);
        });

        stats.getStat(PetStats.STAT_CLEANLINESS).addListener((obs, oldVal, newVal) -> {
            double percentage = newVal.intValue() / 100.0;
            cleanFillRect.setWidth(150 * percentage);
        });

        // Initial update
        hungerFillRect.setWidth(150 * stats.getStat(PetStats.STAT_HUNGER).get() / 100.0);
        happinessFillRect.setWidth(225 * stats.getStat(PetStats.STAT_HAPPINESS).get() / 100.0);
        energyFillRect.setWidth(150 * stats.getStat(PetStats.STAT_ENERGY).get() / 100.0);
        cleanFillRect.setWidth(150 * stats.getStat(PetStats.STAT_CLEANLINESS).get() / 100.0);
    }

    // NEW: Start random pet image switching
    private void startPetImageSwitching() {
        if (petImageView == null || petImage1 == null || petImage2 == null || petImage3 == null) return;

        petImageSwitcher = new Timeline(new KeyFrame(Duration.seconds(getRandomInterval()), _ -> {
            switchPetImage();
            // Reschedule with new random interval
            petImageSwitcher.stop();
            startPetImageSwitching();
        }));
        petImageSwitcher.play();
    }

    // NEW: Switch between pet images randomly
    private void switchPetImage() {
        if (petImageView == null) return;
        
        Image currentImage = petImageView.getImage();
        
        if (isCrying) {
            // Switch between sleepy images
            if (currentImage == cryingBear1) {
                petImageView.setImage(cryingBear2);
            } 
            else {
                petImageView.setImage(cryingBear1);
            }
        } else {
            // Switch between normal images
            if (currentImage == petImage1) {
                petImageView.setImage(petImage2);
            } else if (currentImage == petImage2) {
                petImageView.setImage(petImage3);
            } else {
                petImageView.setImage(petImage1);
            }
        }
    }

    // NEW: Generate random interval between 3-10 seconds
    private double getRandomInterval() {
        if (isCrying) {
            return 0.5 + random.nextDouble() * 1.0; // Fast: 0.5-1.5 seconds when sleepy
        }
        return 3 + random.nextDouble() * 7; // Normal: 3-10 seconds when awake
    }

    // Just a dummy to show crying state
    private void toggleCryingState() {
        isCrying = !isCrying;
        
        // Stop current animation
        if (petImageSwitcher != null) {
            petImageSwitcher.stop();
        }
        
        // Set initial image for new state
        if (isCrying) {
            petImageView.setImage(cryingBear1);
        } else {
            petImageView.setImage(petImage1);
        }
        
        // Restart animation with new interval
        startPetImageSwitching();
    }
}


