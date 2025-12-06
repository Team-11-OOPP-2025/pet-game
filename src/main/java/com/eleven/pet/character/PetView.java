package com.eleven.pet.character;

import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.DayCycle;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.SpriteSheetAnimation;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
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
    private Image sadBear;
    private Image sadBear1;
    private Image sadBear2;
    private Image sleepingBear1;
    private Image sleepingBear2;
    private Image happyBear1;
    private Image happyBear2;
    private Image happyBearLookingRight;
    private Image happyBearLookingLeft;
    private Random random = new Random();
    private boolean isCrying = false;
    private boolean isSad = false;
    private boolean isHappy = false;
    private boolean isSleeping = false;
    private boolean isShowingSadBear1 = true; // Track which sad bear is showing
    private AnimationState currentAnimationState = AnimationState.NEUTRAL;

    // NEW: SpriteSheetAnimation fields
    private SpriteSheetAnimation neutralAnimation;
    private SpriteSheetAnimation happyAnimation;
    private SpriteSheetAnimation sadAnimation;
    private SpriteSheetAnimation cryingAnimation;
    private SpriteSheetAnimation sleepingAnimation;
    private SpriteSheetAnimation currentAnimation;
    private AnimationTimer animationTimer;
    private long lastUpdateTime;
    private Image currentSpriteSheet;
    private Image sleepingBear;
    
    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weather) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weather;
        this.assetLoader = AssetLoader.getInstance();
        loadBackgroundImages();
        loadSpriteSheets();
        initializeSpriteSheetAnimations();
        }
    
    private void loadBackgroundImages() {
        AssetLoader loader = AssetLoader.getInstance();
        earlyMorningBackground = loader.getImage("backgrounds/Dawn");
        lateMorningBackground = loader.getImage("backgrounds/Morning");
        dayBackground = loader.getImage("backgrounds/Day");
        eveningBackground = loader.getImage("backgrounds/Evening");
        earlyNightBackground = loader.getImage("backgrounds/EarlyNight");
        deepNightBackground = loader.getImage("backgrounds/DeepNight");
    
        // Fallback if DeepNight doesn't exist
        if (deepNightBackground == null && dayBackground != null) {
            deepNightBackground = dayBackground;
        }
    }
    
    private void loadSpriteSheets() {
        AssetLoader loader = AssetLoader.getInstance();
        sleepingBear = loader.getImage("pet/sleeping/SpriteSheetSleeping");
        neutralBear = loader.getImage("pet/idle/SpriteSheetNeutral");
        sadBear = loader.getImage("pet/sad/SpriteSheetSad");
        
    }

    // NEW: Initialize sprite sheet animations
    private void initializeSpriteSheetAnimations() {
        // Create animations for different states
        // Assuming each animation has frames arranged horizontally in the sprite sheet
        // Adjust parameters based on actual sprite sheet layout

        // Neutral: 3 frames (neutral, lookLeft, lookRight), 0.5s per frame
        neutralAnimation = new SpriteSheetAnimation(400, 400, 2, 3, 0.5f);
        neutralAnimation.setLoop(true);

        // Happy: 4 frames, faster animation
        happyAnimation = new SpriteSheetAnimation(400, 400, 4, 4, 0.3f);
        happyAnimation.setLoop(true);

        // Sad: 2 frames, slower with custom timing handled separately
        sadAnimation = new SpriteSheetAnimation(400, 400, 2, 2, 0.5f);
        sadAnimation.setLoop(true);

        // Crying: 2 frames, fast
        cryingAnimation = new SpriteSheetAnimation(400, 400, 2, 2, 0.7f);
        cryingAnimation.setLoop(true);

        // Sleeping: 2 frames, slow breathing
        sleepingAnimation = new SpriteSheetAnimation(400, 400, 2, 2, 1.5f);
        sleepingAnimation.setLoop(true);

        // Start with neutral animation
        currentAnimation = neutralAnimation;
        currentAnimation.play();
    }

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
        StackPane.setMargin(petImageView, new Insets(0, 0, 20, 0));
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

        // Start animation timer instead of Timeline
        startAnimationTimer();

        return root;
    }

    private ImageView createPetImage() {
        ImageView imageView = new ImageView();
        imageView.setImage(neutralBear);
        imageView.setFitWidth(400);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);

        // Make pet clickable to toggle sleepy state
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

        button.setOnAction(_ -> {
            if (controller != null) {
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
                controller.handleCleanAction();
            }
        });

        return container;
    }

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
                controller.handleSleepAction();
            }
        });

        return container;
    }

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

        button.setOnAction(_ -> {
            if (controller != null) {
                controller.handlePlayAction();
            }
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
        int hours = (int) gameTime % 24;

        String timeString = String.format("%02d:00", hours);
        timeLabel.setText(timeString);
    }

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

    private void bindStatBarsToModel() {
        if (model == null || model.getStats() == null) return;

        PetStats stats = model.getStats();

        var hungerStat = stats.getStat(PetStats.STAT_HUNGER);
        if (hungerStat != null) {
            hungerStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                hungerFillRect.setWidth(150 * percentage);
            });
            hungerFillRect.setWidth(150 * hungerStat.get() / 100.0);
        }

        var happinessStat = stats.getStat(PetStats.STAT_HAPPINESS);
        if (happinessStat != null) {
            happinessStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                happinessFillRect.setWidth(225 * percentage);

                updateAnimationState(newVal.intValue());
            });
            happinessFillRect.setWidth(225 * happinessStat.get() / 100.0);
            updateAnimationState(happinessStat.get());
        }

        var energyStat = stats.getStat(PetStats.STAT_ENERGY);
        if (energyStat != null) {
            energyStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                energyFillRect.setWidth(150 * percentage);
            });
            energyFillRect.setWidth(150 * energyStat.get() / 100.0);
        }

        var cleanlinessStat = stats.getStat(PetStats.STAT_CLEANLINESS);
        if (cleanlinessStat != null) {
            cleanlinessStat.addListener((obs, oldVal, newVal) -> {
                double percentage = newVal.intValue() / 100.0;
                cleanFillRect.setWidth(150 * percentage);
            });
            cleanFillRect.setWidth(150 * cleanlinessStat.get() / 100.0);
        }
    }

    private void updateAnimationState(int happiness) {
        AnimationState newState = AnimationState.fromHappiness(happiness);
        return;
        }
    

    private void updatePetImageFromAnimation() {
        if (currentAnimation == null || petImageView == null) return;

        int frameX = currentAnimation.getFrameX();
        int frameY = currentAnimation.getFrameY();

        petImageView.setViewport(new Rectangle2D(frameX, frameY,
                currentAnimation.getFrameWidth(), currentAnimation.getFrameHeight()));

        Image targetSheet = getSpriteSheetForCurrentState();
        if (targetSheet != currentSpriteSheet) {
            currentSpriteSheet = targetSheet;
            petImageView.setImage(currentSpriteSheet);
        }
    }

    private Image getSpriteSheetForCurrentState() {
        return null;
    }

    private void switchAnimation(SpriteSheetAnimation newAnimation) {
        if (currentAnimation != null) {
            currentAnimation.pause();
        }

        currentAnimation = newAnimation;
        currentAnimation.reset();
        currentAnimation.play();
    }

    // NEW: Start animation timer for sprite sheet updates
    private void startAnimationTimer() {
        lastUpdateTime = System.nanoTime();
        
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                float deltaTime = (now - lastUpdateTime) / 1_000_000_000.0f; // Convert to seconds
                lastUpdateTime = now;
                
                if (currentAnimation != null) {
                    currentAnimation.update(deltaTime);
                    updatePetImageFromAnimation();
                }
            }
        };
        
        animationTimer.start();
    }

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

        if (model != null) {
            model.getStateProperty().addListener((obs, oldState, newState) -> {
                if (newState != null) {
                    updateAnimationForState(newState.getStateName());

                    if (sleepButtonContainer != null) {
                        boolean isAsleep = "ASLEEP".equals(newState.getStateName());
                        sleepButtonContainer.setDisable(isAsleep);
                        sleepButtonContainer.setOpacity(isAsleep ? 0.5 : 1.0);
                    }
                }
            });
        }
    }

    private void updateAnimationForState(String stateName) {
        if ("ASLEEP".equals(stateName)) {
            isSleeping = true;
            isCrying = false;
            isSad = false;
            isHappy = false;
            switchAnimation(sleepingAnimation);
            currentAnimationState = AnimationState.NEUTRAL; // Reset state
        } else if ("AWAKE".equals(stateName)) {
            isSleeping = false;
            if (model != null && model.getStats() != null) {
                var happinessStat = model.getStats().getStat(PetStats.STAT_HAPPINESS);
                if (happinessStat != null) {
                    updateAnimationState(happinessStat.get());
                }
            }
        }
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

        double hour = normalizedTime * 24.0;

        boolean isSleepTime = (hour >= 20.0 && hour < 24.0) || (hour >= 0.0 && hour < 8.0);
        sleepButtonContainer.setVisible(isSleepTime);
    }
}




