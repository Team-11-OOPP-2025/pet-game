package com.eleven.pet.view;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.controller.PetController;
import com.eleven.pet.data.ItemRegistry;
import com.eleven.pet.environment.clock.DayCycle;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.MinigameResult;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.particle.ParticleSystem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.Random;

/**
 * PetView - UML-compliant implementation integrating existing UI design
 */
public class PetView {
    // Enum for animation states
    private enum AnimationState {
        VERY_HAPPY, NEUTRAL, SAD, VERY_SAD;
        
        static AnimationState fromHappiness(int happiness) {
            if (happiness >= 80) return VERY_HAPPY;
            if (happiness >= 50) return NEUTRAL;
            if (happiness >= 20) return SAD;
            return VERY_SAD;
        }
    }
    
    // UML Fields
    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;
    private final AssetLoader assetLoader;
    private ParticleSystem particleSystem;

    // UI Components (UML standard controls)
    private ImageView petImageView;
    private StackPane backgroundPane;
    private Pane weatherOverlay;
    private ProgressBar hungerBar;
    private ProgressBar happinessBar;
    private ProgressBar energyBar;
    private ProgressBar cleanlinessBar;
    private Button feedButton;
    private Button sleepButton;
    private Button playButton;
    private Button cleanButton;
    private ImageView saveIcon;
    private Label weatherLabel;
    private Label timeLabel;
    private StackPane sleepButtonContainer;

    // Legacy fields for existing visual design
    private ImageView backgroundView;
    private Image earlyMorningBackground;
    private Image lateMorningBackground;
    private Image dayBackground;
    private Image eveningBackground;
    private Image earlyNightBackground;
    private Image deepNightBackground;
    private StackPane feedButtonContainer;
    private StackPane cleanButtonContainer;
    private StackPane playButtonContainer;
    private Text foodCounterText;
    private Rectangle hungerFillRect;
    private Rectangle energyFillRect;
    private Rectangle cleanFillRect;
    private Rectangle happinessFillRect;
    private Image neutralBear;
    private Image neutralBearLookingRight;
    private Image neutralBearLookingLeft;
    private Image cryingBear1;
    private Image cryingBear2;
    private Image sadBear1;
    private Image sadBear2;
    private Image sleepingBear1;
    private Image sleepingBear2;
    private Image happyBear1;
    private Image happyBear2;
    private Image happyBearLookingRight;
    private Image happyBearLookingLeft;
    private Timeline petImageSwitcher;
    private Random random = new Random();
    private boolean isCrying = false;
    private boolean isSad = false;
    private boolean isHappy = false;
    private boolean isSleeping = false;
    private boolean isShowingSadBear1 = true; // Track which sad bear is showing
    private AnimationState currentAnimationState = AnimationState.NEUTRAL;

    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weather) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weather;
        this.assetLoader = AssetLoader.getInstance();
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
        neutralBearLookingLeft = loader.getImage("LookingLeftBear");
        neutralBearLookingRight = loader.getImage("LookingRightBear");
        neutralBear = loader.getImage("Bear");
        cryingBear1 = loader.getImage("CryingBear1");
        cryingBear2 = loader.getImage("CryingBear2");
        sadBear1 = loader.getImage("SadBear1");
        sadBear2 = loader.getImage("SadBear2");
        sleepingBear1 = loader.getImage("SleepingBear1");
        sleepingBear2 = loader.getImage("SleepingBear2");
        happyBear1 = loader.getImage("HappyBear1");
        happyBear2 = loader.getImage("HappyBear2");
        happyBearLookingRight = loader.getImage("HappyBearLookingRight");
        happyBearLookingLeft = loader.getImage("HappyBearLookingLeft");

    }

    // YOUR LAYOUT CODE - KEPT AS-IS!
    public Pane initializeUI() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        backgroundView = new ImageView();

        // Observe environment (clock/weather)
        observeEnvironment();
        if (clock == null && backgroundView != null) {
            backgroundView.setImage(dayBackground);
        }

        backgroundView.setPreserveRatio(false);
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

        cleanButtonContainer = createCleanButton();
        StackPane.setAlignment(cleanButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(cleanButtonContainer, new Insets(20, 20, 90, 150));
        root.getChildren().add(cleanButtonContainer);

        sleepButtonContainer = createSleepButton();
        StackPane.setAlignment(sleepButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(sleepButtonContainer, new Insets(20, 20, 150, 20));
        sleepButtonContainer.setVisible(false); // Initially hidden
        root.getChildren().add(sleepButtonContainer);

        playButtonContainer = createPlayButton();  // Updated name
        StackPane.setAlignment(playButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(playButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(playButtonContainer);

        HBox foodCounter = createFoodCounter();
        StackPane.setAlignment(foodCounter, Pos.TOP_RIGHT);
        StackPane.setMargin(foodCounter, new Insets(90, 20, 0, 0));
        root.getChildren().add(foodCounter);

        // Add digital clock
        Label clockLabel = createDigitalClock();
        StackPane.setAlignment(clockLabel, Pos.TOP_CENTER);
        StackPane.setMargin(clockLabel, new Insets(20, 0, 0, 0));
        root.getChildren().add(clockLabel);

        // Bind UI to model
        bindToModel();

        // Start random pet image switching
        startPetImageSwitching();

        return root;
    }

    // YOUR EXACT CODE - KEPT AS-IS!
    private ImageView createPetImage() {
        ImageView imageView = new ImageView();
        imageView.setImage(neutralBear);
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        // Make pet clickable to toggle sleepy state
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
                // TODO: Link feed counter to actual food in inventory


                controller.handleFeedAction();
            }
        });

        return container;
    }

    private StackPane createCleanButton() {
        StackPane container = new StackPane();
        container.setPrefSize(120, 50);
        container.setMaxSize(120, 50);
        container.setMinSize(120, 50);

        Rectangle bgRect = new Rectangle(120, 50);
        bgRect.setFill(Color.WHITE);
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);

        Button button = new Button("CLEAN");
        button.setPrefSize(120, 50);
        button.setMaxSize(120, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setTextFill(Color.BLACK);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        container.getChildren().addAll(bgRect, button);

        button.setOnAction(_ -> {
            if (controller != null) {
                controller.handleClean();
            }
        });

        return container;
    }

    // Sleep button for night time
    private StackPane createSleepButton() {
        StackPane container = new StackPane();
        container.setPrefSize(120, 50);
        container.setMaxSize(120, 50);
        container.setMinSize(120, 50);

        Rectangle bgRect = new Rectangle(120, 50);
        bgRect.setFill(Color.web("#3498db")); // Blue color for sleep
        bgRect.setStroke(Color.BLACK);
        bgRect.setStrokeWidth(3);

        Button button = new Button("SLEEP");
        button.setPrefSize(120, 50);
        button.setMaxSize(120, 50);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setTextFill(Color.WHITE);
        button.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        container.getChildren().addAll(bgRect, button);

        button.setOnAction(_ -> {
            if (controller != null) {
                controller.handleSleepButton();
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


        if (model != null && model.getInventory() != null) {
            foodCounterText.setText(String.valueOf(model.getInventory().getQuantity(ItemRegistry.get(0))));
            model.getInventory().amountProperty(ItemRegistry.get(0)).addListener((_, _, _) -> {
                foodCounterText.setText(String.valueOf(model.getInventory().getQuantity(ItemRegistry.get(0))));
            });
        }

        return container;
    }

    private Label createDigitalClock() {
        timeLabel = new Label("00:00");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        timeLabel.setTextFill(Color.WHITE);
        timeLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10; -fx-padding: 10 20;");

        if (clock != null) {
            updateClockDisplay();
            clock.gameTimeProperty().addListener((_, _, _) -> updateClockDisplay());
        }

        return timeLabel;
    }

    private void updateClockDisplay() {
        if (clock == null || timeLabel == null) return;

        double gameTime = clock.getGameTime();
        // DAY_LENGTH_SECONDS is 24 seconds = full day (24 hours)
        // So each second = 1 hour in game time
        int hours = (int) gameTime % 24;

        String timeString = String.format("%02d:00", hours);
        timeLabel.setText(timeString);
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
    // Moved to updateBaseBackground (UML method)

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
        if (model == null || model.getStats() == null) return;

        PetStats stats = model.getStats();

        // Bind each bar to its stat (0-100 mapped to width) with null checks
        var hungerStat = stats.getStat(PetStats.STAT_HUNGER);
        if (hungerStat != null) {
            hungerStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                hungerFillRect.setWidth(150 * percentage);
            });
            // Initial update
            hungerFillRect.setWidth(150 * hungerStat.get() / 100.0);
        }

        var happinessStat = stats.getStat(PetStats.STAT_HAPPINESS);
        if (happinessStat != null) {
            happinessStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                happinessFillRect.setWidth(225 * percentage);

                // Update animation state based on happiness
                updateAnimationState(newVal.intValue());
            });
            // Initial update
            happinessFillRect.setWidth(225 * happinessStat.get() / 100.0);
            updateAnimationState(happinessStat.get());
        }

        var energyStat = stats.getStat(PetStats.STAT_ENERGY);
        if (energyStat != null) {
            energyStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                energyFillRect.setWidth(150 * percentage);
            });
            // Initial update
            energyFillRect.setWidth(150 * energyStat.get() / 100.0);
        }

        var cleanlinessStat = stats.getStat(PetStats.STAT_CLEANLINESS);
        if (cleanlinessStat != null) {
            cleanlinessStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                cleanFillRect.setWidth(150 * percentage);
            });
            // Initial update
            cleanFillRect.setWidth(150 * cleanlinessStat.get() / 100.0);
        }
    }

    // NEW: Update animation state based on happiness level
    private void updateAnimationState(int happiness) {
        AnimationState newState = AnimationState.fromHappiness(happiness);

        if (newState != currentAnimationState) {
            currentAnimationState = newState;

            // Stop current animation
            if (petImageSwitcher != null) {
                petImageSwitcher.stop();
            }

            // Set initial image for new state
            switch (currentAnimationState) {
                case VERY_HAPPY:
                    isSleeping = false;
                    isCrying = false;
                    isSad = false;
                    isHappy = true;
                    petImageView.setImage(happyBear1);
                    break;
                case NEUTRAL:
                    isSleeping = false;
                    isCrying = false;
                    isSad = false;
                    isHappy = false;
                    petImageView.setImage(neutralBear);
                    break;
                case SAD:
                    isSleeping = false;
                    isCrying = false;
                    isSad = true;
                    isHappy = false;
                    petImageView.setImage(sadBear1);
                    isShowingSadBear1 = true;
                    break;
                case VERY_SAD:
                    isSleeping = false;
                    isCrying = true;
                    isSad = false;
                    isHappy = false;
                    petImageView.setImage(cryingBear1);
                    isShowingSadBear1 = true;
                    break;
            }

            // Restart animation with new state
            startPetImageSwitching();
        }
    }

    // NEW: Start random pet image switching
    private void startPetImageSwitching() {
        if (petImageView == null || neutralBear == null || neutralBearLookingLeft == null || neutralBearLookingRight == null)
            return;

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

        if (isSleeping) {
            // Switch between sleeping images
            if (currentImage == sleepingBear1) {
                petImageView.setImage(sleepingBear2);
            } else {
                petImageView.setImage(sleepingBear1);
            }
        } else if (isCrying) {
            // Switch between crying images
            if (currentImage == cryingBear1) {
                petImageView.setImage(cryingBear2);
            } else {
                petImageView.setImage(cryingBear1);
            }
        } else if (isSad || currentAnimationState == AnimationState.SAD) {
            // Switch between semi-sad images and track which one is showing
            if (currentImage == sadBear1) {
                petImageView.setImage(sadBear2);
                isShowingSadBear1 = false;
            } else {
                petImageView.setImage(sadBear1);
                isShowingSadBear1 = true;
            }
        } else if (isHappy || currentAnimationState == AnimationState.VERY_HAPPY) {
            // Switch between happy images
            if (currentImage == happyBear1) {
                petImageView.setImage(happyBearLookingLeft);
            } else if (currentImage == happyBearLookingLeft) {
                petImageView.setImage(happyBearLookingRight);
            } else if (currentImage == happyBearLookingRight) {
                petImageView.setImage(happyBear2);
            } else {
                petImageView.setImage(happyBear1);
            }
        } else {
            // Switch between normal images (NEUTRAL)
            if (currentImage == neutralBear) {
                petImageView.setImage(neutralBearLookingLeft);
            } else if (currentImage == neutralBearLookingLeft) {
                petImageView.setImage(neutralBearLookingRight);
            } else {
                petImageView.setImage(neutralBear);
            }
        }
    }

    // NEW: Generate random interval between 3-10 seconds
    private double getRandomInterval() {
        if (isSleeping) {
            return 1.0 + random.nextDouble() * 1.5; // Slow breathing: 1.0-2.5 seconds when sleeping
        }
        if (isCrying) {
            return 0.5 + random.nextDouble() * 1.0; // Fast: 0.5-1.5 seconds when crying
        }
        if (isSad || currentAnimationState == AnimationState.SAD) {
            // sadBear1 shows longer (3-4 seconds), sadBear2 shows shorter (0.5-1 second)
            if (isShowingSadBear1) {
                return 3.0 + random.nextDouble() * 1.0; // 3-4 seconds for sadBear1
            } else {
                return 0.5; // 0.5-1 second for sadBear2
            }
        }

        // Adjust speed based on happiness level
        switch (currentAnimationState) {
            case VERY_HAPPY:
                return 1.5 + random.nextDouble() * 2.0; // Faster: 1.5-3.5 seconds
            case NEUTRAL:
            default:
                return 3 + random.nextDouble() * 7; // Normal: 3-10 seconds
        }
    }


    // ========== UML Methods (Skeleton Implementation) ==========

    public void showSaveIcon(boolean visible) {
        // TODO: Implement save icon visibility toggle
    }

    public void promptSleep() {
        // TODO: Implement sleep prompt dialog
    }

    public void showMinigameResult(MinigameResult result) {
        // TODO: Implement minigame result display
    }

    private HBox createTopPanel() {
        // TODO: Implement top panel with weather and time labels
        return new HBox();
    }

    private VBox createStatsPanel() {
        // TODO: Implement stats panel with progress bars
        return new VBox();
    }

    private VBox createStatRow(String label, ProgressBar bar) {
        // TODO: Implement stat row layout
        return new VBox();
    }

    private ProgressBar createStatBar(String name) {
        // TODO: Implement progress bar creation
        return new ProgressBar();
    }

    private HBox createButtonPanel() {
        // TODO: Implement button panel with all action buttons
        return new HBox();
    }

    private void setupEventHandlers() {
        // TODO: Implement event handler setup
    }

    private void bindToModel() {
        bindStatBarsToModel();
        
        // Listen to state changes to update animations
        if (model != null) {
            model.getStateProperty().addListener((obs, oldState, newState) -> {
                if (newState != null) {
                    updateAnimationForState(newState.getStateName());
                    
                    // Disable sleep button when asleep, enable when awake
                    if (sleepButtonContainer != null) {
                        boolean isAsleep = "asleep".equals(newState.getStateName());
                        sleepButtonContainer.setDisable(isAsleep);
                        sleepButtonContainer.setOpacity(isAsleep ? 0.5 : 1.0);
                    }
                }
            });
        }
    }

    // NEW: Update animation based on state name
    private void updateAnimationForState(String stateName) {
        if (petImageSwitcher != null) {
            petImageSwitcher.stop();
        }
        
        if ("asleep".equals(stateName)) {
            // Switch to sleeping animation
            isSleeping = true;
            isCrying = false;
            isSad = false;
            isHappy = false;
            petImageView.setImage(sleepingBear1);
            currentAnimationState = AnimationState.NEUTRAL; // Reset state
        } else if ("awake".equals(stateName)) {
            // Return to normal animation based on happiness
            isSleeping = false;
            if (model != null && model.getStats() != null) {
                var happinessStat = model.getStats().getStat(PetStats.STAT_HAPPINESS);
                if (happinessStat != null) {
                    updateAnimationState(happinessStat.get());
                }
            }
        }
        
        startPetImageSwitching();
    }

    private void observeEnvironment() {
        if (clock != null) {
            updateBaseBackground(clock.getCycle());
            updateSleepButtonVisibility();

            clock.cycleProperty().addListener((_, _, newCycle) -> {
                updateBaseBackground(newCycle);
            });

            clock.gameTimeProperty().addListener((_, _, _) -> {
                updateSleepButtonVisibility();
            });
        }
    }

    private void updateVisuals() {
        // TODO: Implement visual update logic
    }

    private void updatePetSprite() {
        // TODO: Implement pet sprite update based on state
    }

    private void updateBaseBackground(DayCycle cycle) {
        if (backgroundView == null) return;

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

    private void updateWeatherOverlay(WeatherState weather) {
        // TODO: Implement weather overlay update
    }

    private void updateSleepButtonVisibility() {
        if (clock == null || sleepButtonContainer == null) return;

        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;

        // Calculate hour (0-23)
        double hour = normalizedTime * 24.0;

        // Show sleep button between 20:00-24:00 and 00:00-08:00
        boolean isSleepTime = (hour >= 20.0 && hour < 24.0) || (hour >= 0.0 && hour < 8.0);
        sleepButtonContainer.setVisible(isSleepTime);
    }
}




