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
import java.util.Map;
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
    // Remove individual background image fields - no longer needed
    private StackPane feedButtonContainer;
    private StackPane cleanButtonContainer;
    private StackPane playButtonContainer;
    private Text foodCounterText;
    private Rectangle hungerFillRect;
    private Rectangle energyFillRect;
    private Rectangle cleanFillRect;
    private Rectangle happinessFillRect;
    private Image neutralBear;
    private Image sadBear;
    
    private AnimationState currentAnimationState = AnimationState.NEUTRAL;

    // NEW: SpriteSheetAnimation fields
    private SpriteSheetAnimation NEUTRAL;
    private SpriteSheetAnimation VERY_HAPPY;
    private SpriteSheetAnimation SAD;
    private SpriteSheetAnimation VERY_SAD;
    private SpriteSheetAnimation sleepingAnimation;
    private SpriteSheetAnimation currentAnimation;
    private AnimationTimer animationTimer;
    private long lastUpdateTime;
    private Image currentSpriteSheet;
    private Image sleepingBear;
    private Image cryingBear;

    // NEW: Map for state-to-sprite mapping
    private Map<AnimationState, Image> animationStateToSpriteMap;

    private Image DAWN;
    private Image MORNING;
    private Image DAY;
    private Image EVENING;
    private Image EARLY_NIGHT;
    private Image DEEP_NIGHT;
   

    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weather) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weather;
        this.assetLoader = AssetLoader.getInstance();
        loadSpriteSheets();
        initializeSpriteSheetAnimations();
        initializeAnimationStateMap();
    }
    
    // Remove loadBackgroundImages() method entirely
    private void loadBackGroundImages() {
        AssetLoader loader = AssetLoader.getInstance();
        DAWN = loader.getImage("backgrounds/DAWN");
        MORNING = loader.getImage("backgrounds/MORNING");
        DAY = loader.getImage("backgrounds/DAY");
        EVENING = loader.getImage("backgrounds/EVENING");
        EARLY_NIGHT = loader.getImage("backgrounds/EARLY_NIGHT");
        DEEP_NIGHT = loader.getImage("backgrounds/DEEP_NIGHT");
    }
    
    private void loadSpriteSheets() {
        AssetLoader loader = AssetLoader.getInstance();
        sleepingBear = loader.getImage("pet/sleeping/SpriteSheetSleeping");
        neutralBear = loader.getImage("pet/idle/SpriteSheetNeutral");
        sadBear = loader.getImage("pet/sad/SpriteSheetSad");
        cryingBear = loader.getImage("pet/sad/SpriteSheetCrying");
    }

    // NEW: Initialize sprite sheet animations
    private void initializeSpriteSheetAnimations() {

        // Neutral: 3 frames (neutral, lookLeft, lookRight), 0.5s per frame
        NEUTRAL = new SpriteSheetAnimation(309, 460, 2, 3, 1f);
        NEUTRAL.setLoop(true);

        // Happy: 4 frames, faster animation
        VERY_HAPPY = new SpriteSheetAnimation(309, 460, 4, 4, 0.3f);
        VERY_HAPPY.setLoop(true);

        // Sad: 2 frames, slower with custom timing handled separately
        SAD = new SpriteSheetAnimation(309, 460, 1, 2, 0.5f);
        SAD.setLoop(true);

        // Crying: 2 frames, fast
        VERY_SAD = new SpriteSheetAnimation(309, 460, 1, 2, 0.7f);
        VERY_SAD.setLoop(true);

        // Sleeping: 2 frames, slow breathing
        sleepingAnimation = new SpriteSheetAnimation(309, 460, 1, 2, 1.5f);
        sleepingAnimation.setLoop(true);

        // Start with neutral animation
        currentAnimation = NEUTRAL;
        currentAnimation.play();
    }

    private void initializeAnimationStateMap() {
        animationStateToSpriteMap = Map.of(
            AnimationState.VERY_HAPPY, neutralBear,
            AnimationState.NEUTRAL, neutralBear,
            AnimationState.SAD, sadBear,
            AnimationState.VERY_SAD, cryingBear
        );
    }

    public Pane initializeUI() {
        StackPane root = new StackPane();
        
        backgroundView = new ImageView();
        
        // Setup background view properties
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(root.widthProperty());
        backgroundView.fitHeightProperty().bind(root.heightProperty());
        root.getChildren().add(backgroundView);

        // Set initial background using the current clock state
        if (clock != null) {
            updateBackground(clock.getCycle()); 
        } else {
            // Fallback default if clock is null (e.g. Day)
            backgroundView.setImage(DAY); 
        }
        
        observeEnvironment();
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
        imageView.setFitWidth(309);
        imageView.setFitHeight(460);
        imageView.setPreserveRatio(true);

        
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

    private void updateBackground(DayCycle cycle) {
        // Dynamically construct the asset path based on the enum name.
        // E.g. If cycle is DayCycle.DAWN, this looks for "backgrounds/dawn"
        String assetPath = "backgrounds/" + cycle.name();
        
        Image bgImage = assetLoader.getImage(assetPath);
        
        // Only update if the image was successfully found
        if (bgImage != null) {
            backgroundView.setImage(bgImage);
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
        
        // Only update if the mood changed AND we aren't in a blocking state (like sleeping)
        if (currentAnimationState != newState) {
            currentAnimationState = newState;
            
            // If the pet is currently just IDLE (not sleeping), update the visual immediately
            if (model != null && "IDLE".equals(model.getCurrentState().getStateName())) {
                updateAnimationForState("IDLE");
            }
        }
    }
    

    private void updatePetImageFromAnimation() {
        if (currentAnimation == null || petImageView == null) return;

        int frameX = currentAnimation.getFrameX();
        int frameY = currentAnimation.getFrameY();

        petImageView.setViewport(new Rectangle2D(frameX, frameY,
                currentAnimation.getFrameWidth(), currentAnimation.getFrameHeight()));

        Image targetSheet = getCurrentAnimation();
        if (targetSheet != currentSpriteSheet) {
            currentSpriteSheet = targetSheet;
            petImageView.setImage(currentSpriteSheet);
        }
    }

    private Image getCurrentAnimation() {
        String state = model != null ? model.getCurrentState().getStateName() : "IDLE";
        return state.equals("ASLEEP") 
            ? sleepingBear 
            : animationStateToSpriteMap.getOrDefault(currentAnimationState, neutralBear);
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
        Map<String, SpriteSheetAnimation> stateAnimations = Map.of(
            "ASLEEP", sleepingAnimation,
            "IDLE", animationStateToSpriteMap.containsKey(currentAnimationState) 
                ? Map.of(AnimationState.VERY_HAPPY, VERY_HAPPY, 
                         AnimationState.NEUTRAL, NEUTRAL,
                         AnimationState.SAD, SAD,
                         AnimationState.VERY_SAD, VERY_SAD)
                    .getOrDefault(currentAnimationState, NEUTRAL)
                : NEUTRAL
        );
        
        SpriteSheetAnimation targetAnimation = stateAnimations.getOrDefault(stateName, NEUTRAL);
        switchAnimation(targetAnimation);
    }

    private void observeEnvironment() {
        if (clock != null) {
            updateSleepButtonVisibility();

            // Bind the updateBackground function to the clock cycle property
            clock.cycleProperty().addListener((_, _, newCycle) -> {
                updateBackground(newCycle);
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




