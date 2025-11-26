package com.eleven.pet.view;

import com.eleven.pet.controller.PetController;
import com.eleven.pet.environment.time.DayCycle;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;          //new Idk if right
import com.eleven.pet.particle.ParticleSystem;
import com.eleven.pet.util.AssetLoader;

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

public class PetView {
    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;
    private final WeatherSystem weatherSystem;
    private final AssetLoader assetLoader;
    private final ParticleSystem particleSystem;
    
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
    private Image petImage1;
    private Image petImage2;
    private boolean showingFirstImage = true;
    
    public PetView(PetModel model, PetController controller, GameClock clock, WeatherSystem weather) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;
        this.weatherSystem = weather;
        this.assetLoader = AssetLoader.getInstance();
        this.particleSystem = new ParticleSystem(800, 600);
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
        
        if (clock != null) {
            DayCycle initialCycle = clock.getCycle();
            updateBackground(initialCycle == DayCycle.DAY);
            clock.cycleProperty().addListener((obs, oldVal, newVal) -> {
                updateBackground(newVal == DayCycle.DAY);
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
        
        StackPane happinessBarPane = createHappinessBar();
        StackPane.setAlignment(happinessBarPane, Pos.TOP_LEFT);
        StackPane.setMargin(happinessBarPane, new Insets(90, 20, 20, 20));
        root.getChildren().add(happinessBarPane);
        
        StackPane hungerBarPane = createHungerBar();
        StackPane.setAlignment(hungerBarPane, Pos.TOP_LEFT);
        StackPane.setMargin(hungerBarPane, new Insets(148, 20, 20, 20));
        root.getChildren().add(hungerBarPane);

        StackPane energyBarPane = createEnergyBar();
        StackPane.setAlignment(energyBarPane, Pos.TOP_LEFT);
        StackPane.setMargin(energyBarPane, new Insets(193, 20, 20, 20));
        root.getChildren().add(energyBarPane);
        
        StackPane cleanBarPane = createCleanBar();
        StackPane.setAlignment(cleanBarPane, Pos.TOP_LEFT);
        StackPane.setMargin(cleanBarPane, new Insets(238, 20, 20, 20));
        root.getChildren().add(cleanBarPane);
        
        feedButtonContainer = createFeedButton();
        StackPane.setAlignment(feedButtonContainer, Pos.BOTTOM_LEFT);
        StackPane.setMargin(feedButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(feedButtonContainer);
        
        // Add food counter text (showing inventory count)
        com.eleven.pet.model.FoodItem basicFood = new com.eleven.pet.model.FoodItem("Basic Food", 20);
        foodCounterText = new Text("Food: " + model.getInventory().getQuantity(basicFood));
        foodCounterText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        foodCounterText.setFill(Color.WHITE);
        foodCounterText.setStroke(Color.BLACK);
        foodCounterText.setStrokeWidth(1);
        StackPane.setAlignment(foodCounterText, Pos.TOP_RIGHT);
        StackPane.setMargin(foodCounterText, new Insets(20, 20, 50, 30));
        root.getChildren().add(foodCounterText);
        
        // Bind food counter to model inventory
        model.getInventory().getQuantityProperty(basicFood).addListener((obs, oldVal, newVal) -> {
            foodCounterText.setText("Food: " + newVal);
        });
        
        // Bind stat bars to model
        bindStatBars();
        
        miniGamesButtonContainer = createMiniGamesButton();
        StackPane.setAlignment(miniGamesButtonContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(miniGamesButtonContainer, new Insets(20, 20, 90, 20));
        root.getChildren().add(miniGamesButtonContainer);
        
        return root;
    }
    
    public void showSaveIcon(boolean visible) {
        if (saveIcon != null) {
            saveIcon.setVisible(visible);
        }
    }
    
    private HBox createTopPanel() {
        HBox topPanel = new HBox(20);
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setPadding(new Insets(10));
        
        if (weatherLabel == null) {
            weatherLabel = new Label("Weather: Sunny");
        }
        if (timeLabel == null) {
            timeLabel = new Label("Time: Day");
        }
        
        topPanel.getChildren().addAll(weatherLabel, timeLabel);
        return topPanel;
    }
    
    private VBox createStatsPanel() {
        VBox statsPanel = new VBox(10);
        statsPanel.setPadding(new Insets(10));
        return statsPanel;
    }
    
    private VBox createStatRow(String label, ProgressBar bar) {
        VBox row = new VBox(5);
        Label statLabel = new Label(label);
        row.getChildren().addAll(statLabel, bar);
        return row;
    }
    
    private ProgressBar createStatBar(String name) {
        ProgressBar bar = new ProgressBar(1.0);
        bar.setPrefWidth(200);
        return bar;
    }
    
    private HBox createButtonPanel() {
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER);
        buttonPanel.setPadding(new Insets(10));
        
        if (feedButton == null) feedButton = new Button("Feed");
        if (sleepButton == null) sleepButton = new Button("Sleep");
        if (playButton == null) playButton = new Button("Play");
        if (cleanButton == null) cleanButton = new Button("Clean");
        
        buttonPanel.getChildren().addAll(feedButton, sleepButton, playButton, cleanButton);
        return buttonPanel;
    }
    
    private void setupEventHandlers() {
        if (feedButton != null) {
            feedButton.setOnAction(e -> controller.handleFeedAction());
        }
        if (sleepButton != null) {
            sleepButton.setOnAction(e -> controller.handleSleepAction());
        }
        if (playButton != null) {
            playButton.setOnAction(e -> controller.handlePlayAction());
        }
        if (cleanButton != null) {
            cleanButton.setOnAction(e -> controller.handleCleanAction());
        }
    }
    
    private void bindToModel() {
    }
    
    private void observeEnvironment() {
        if (clock != null) {
            clock.cycleProperty().addListener((obs, oldVal, newVal) -> {
                updateBaseBackground(newVal);
            });
        }
        
        if (weatherSystem != null) {
            weatherSystem.getWeatherProperty().addListener((obs, oldVal, newVal) -> {
                updateWeatherOverlay(newVal);
            });
        }
    }
    
    private void updateVisuals() {
        updatePetSprite();
    }
    
    private void updatePetSprite() {
        if (petImageView != null && model != null) {
        }
    }
    
    private void updateBaseBackground(DayCycle cycle) {
        updateBackground(cycle == DayCycle.DAY);
    }
    
    private void updateWeatherOverlay(WeatherState weather) {
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
                controller.handleFeedAction();
            }
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
        
        Button playBtn = new Button("PLAY");
        playBtn.setPrefSize(140, 50);
        playBtn.setMaxSize(140, 50);
        playBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        playBtn.setTextFill(Color.BLACK);
        playBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        container.getChildren().addAll(bgRect, playBtn);
        
        playBtn.setOnAction(e -> {
            if (controller != null) {
                controller.handlePlayAction();
            }
        });
        
        return container;
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
    
    private void bindStatBars() {
        // Bind hunger bar
        if (model.getStats().getStat(PetStats.STAT_HUNGER) != null) {
            model.getStats().getStat(PetStats.STAT_HUNGER).addListener((obs, oldVal, newVal) -> {
                hungerFillRect.setWidth((newVal.doubleValue() / 100.0) * 150);
            });
            // Set initial value
            hungerFillRect.setWidth((model.getStats().getStat(PetStats.STAT_HUNGER).get() / 100.0) * 150);
        }
        
        // Bind happiness bar
        if (model.getStats().getStat(PetStats.STAT_HAPPINESS) != null) {
            model.getStats().getStat(PetStats.STAT_HAPPINESS).addListener((obs, oldVal, newVal) -> {
                happinessFillRect.setWidth((newVal.doubleValue() / 100.0) * 150);
            });
            happinessFillRect.setWidth((model.getStats().getStat(PetStats.STAT_HAPPINESS).get() / 100.0) * 150);
        }
        
        // Bind energy bar
        if (model.getStats().getStat(PetStats.STAT_ENERGY) != null) {
            model.getStats().getStat(PetStats.STAT_ENERGY).addListener((obs, oldVal, newVal) -> {
                energyFillRect.setWidth((newVal.doubleValue() / 100.0) * 150);
            });
            energyFillRect.setWidth((model.getStats().getStat(PetStats.STAT_ENERGY).get() / 100.0) * 150);
        }
        
        // Bind clean bar
        if (model.getStats().getStat(PetStats.STAT_CLEANLINESS) != null) {
            model.getStats().getStat(PetStats.STAT_CLEANLINESS).addListener((obs, oldVal, newVal) -> {
                cleanFillRect.setWidth((newVal.doubleValue() / 100.0) * 150);
            });
            cleanFillRect.setWidth((model.getStats().getStat(PetStats.STAT_CLEANLINESS).get() / 100.0) * 150);
        }
    }
    
    public void promptSleep() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Sleep Time");
        alert.setHeaderText(model.getName() + " is tired!");
        alert.setContentText("Your pet's energy is low. Consider putting them to sleep.");
        alert.showAndWait();
    }
    
    public void showMinigameResult(com.eleven.pet.model.MinigameResult result) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            result.isWon() ? javafx.scene.control.Alert.AlertType.INFORMATION : javafx.scene.control.Alert.AlertType.WARNING
        );
        alert.setTitle("Minigame Result");
        alert.setHeaderText(result.isWon() ? "Victory!" : "Try Again");
        alert.setContentText(result.getMessage() + "\nHappiness " + (result.getHappinessDelta() > 0 ? "+" : "") + result.getHappinessDelta());
        alert.showAndWait();
    }
}
