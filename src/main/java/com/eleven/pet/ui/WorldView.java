package com.eleven.pet.ui;

import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.environment.time.DayCycle;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.effects.ParticleEffect;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.eleven.pet.ui.ViewConstants.*;

public class WorldView extends StackPane {

    // Layout Constants (Relative to 624x351)
    private static final double WINDOW_X = 230;
    private static final double WINDOW_Y = 20;
    private static final double WINDOW_WIDTH = 120;
    private static final double WINDOW_HEIGHT = 200;

    private static final double TV_X = 462;
    private static final double TV_Y = 142;
    private static final double TV_WIDTH = 112;
    private static final double TV_HEIGHT = 72;

    private static final double CLOCK_X = 480;
    private static final double CLOCK_Y = 95;
    private static final double CLOCK_WIDTH = 100;
    private static final double CLOCK_HEIGHT = 50;

    private final GameClock clock;
    private final AssetLoader assetLoader;
    private final ParticleSystem particleSystem;
    private final WeatherSystem weatherSystem;

    private ImageView backgroundView;
    private StackPane backgroundClockPane;
    private Label backgroundClockLabel;
    private StackPane tvClickArea;
    private Image backgroundDay;

    private ParticleEffect currentWeatherEffect;

    public WorldView(GameClock clock, WeatherSystem weatherSystem) {
        this.clock = clock;
        this.assetLoader = AssetLoader.getInstance();
        this.particleSystem = new ParticleSystem((int) REF_WIDTH, (int) REF_HEIGHT);
        this.weatherSystem = weatherSystem;

        loadAssets();
        setupBackgroundLayer();
        setupParticleLayer();
        setupTVLayer();
        setupBackgroundClock();
        observeEnvironment();
    }

    private void loadAssets() {
        backgroundDay = assetLoader.getImage("backgrounds/DAY");
    }

    private void setupBackgroundLayer() {
        backgroundView = new ImageView();
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(widthProperty());
        backgroundView.fitHeightProperty().bind(heightProperty());

        if (clock != null) updateBackground(clock.getCycle());
        else backgroundView.setImage(backgroundDay);

        getChildren().add(backgroundView);
    }

    private void setupParticleLayer() {
        var canvas = particleSystem.getCanvas();
        canvas.setMouseTransparent(true);

        Pane particlePane = new Pane();
        particlePane.setPickOnBounds(false);
        particlePane.getChildren().add(canvas);

        // Bind scaling logic
        canvas.layoutXProperty().bind(widthProperty().multiply(WINDOW_X / REF_WIDTH));
        canvas.layoutYProperty().bind(heightProperty().multiply(WINDOW_Y / REF_HEIGHT));
        canvas.widthProperty().bind(widthProperty().multiply(WINDOW_WIDTH / REF_WIDTH));
        canvas.heightProperty().bind(heightProperty().multiply(WINDOW_HEIGHT / REF_HEIGHT));

        getChildren().add(particlePane);
    }

    private void setupTVLayer() {
        Pane tvOverlay = new Pane();
        tvOverlay.setPickOnBounds(false);

        tvClickArea = new StackPane();
        tvClickArea.setCursor(Cursor.HAND);

        // Bind scaling logic
        tvClickArea.layoutXProperty().bind(widthProperty().multiply(TV_X / REF_WIDTH));
        tvClickArea.layoutYProperty().bind(heightProperty().multiply(TV_Y / REF_HEIGHT));
        tvClickArea.prefWidthProperty().bind(widthProperty().multiply(TV_WIDTH / REF_WIDTH));
        tvClickArea.prefHeightProperty().bind(heightProperty().multiply(TV_HEIGHT / REF_HEIGHT));

        tvOverlay.getChildren().add(tvClickArea);
        getChildren().add(tvOverlay);
    }

    private void setupBackgroundClock() {
        backgroundClockPane = new StackPane();
        backgroundClockPane.setPickOnBounds(false);

        Image clockFrameImage = assetLoader.getImage("ui/digital-clock");
        if (clockFrameImage != null) {
            ImageView clockFrame = new ImageView(clockFrameImage);
            clockFrame.setPreserveRatio(true);
            clockFrame.setFitWidth(CLOCK_WIDTH);
            backgroundClockPane.getChildren().add(clockFrame);
        }

        backgroundClockLabel = new Label("12:00");
        backgroundClockLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 14));
        backgroundClockLabel.setTextFill(Color.BLACK);
        backgroundClockLabel.setStyle("-fx-background-color: transparent;");
        backgroundClockPane.getChildren().add(backgroundClockLabel);

        Pane clockPane = new Pane();
        clockPane.setPickOnBounds(false);
        clockPane.getChildren().add(backgroundClockPane);

        // Bind scaling logic
        backgroundClockPane.layoutXProperty().bind(widthProperty().multiply(CLOCK_X / REF_WIDTH));
        backgroundClockPane.layoutYProperty().bind(heightProperty().multiply(CLOCK_Y / REF_HEIGHT));
        backgroundClockPane.prefWidthProperty().bind(widthProperty().multiply(CLOCK_WIDTH / REF_WIDTH));
        backgroundClockPane.prefHeightProperty().bind(heightProperty().multiply(CLOCK_HEIGHT / REF_HEIGHT));

        getChildren().add(clockPane);
    }

    private void observeEnvironment() {
        if (clock == null) return;
        clock.cycleProperty().addListener((_, _, cycle) -> updateBackground(cycle));
        clock.gameTimeProperty().addListener((_, _, time) -> updateClockLabel(time.doubleValue()));
        updateClockLabel(clock.getGameTime());
        weatherSystem.getWeatherProperty().addListener((_, _, newWeather) -> {
            updateWeatherEffects(newWeather);
        });

        updateWeatherEffects(weatherSystem.getCurrentWeather());
    }

    private void updateBackground(DayCycle cycle) {
        Image bg = assetLoader.getImage("backgrounds/" + cycle.name());
        if (bg != null) backgroundView.setImage(bg);
    }

    private void updateClockLabel(double time) {
        int hours = (int) time % 24;
        int minutes = (int) ((time % 1.0) * 60);
        String timeString = String.format("%02d:%02d", hours, minutes);
        if (backgroundClockLabel != null) {
            backgroundClockLabel.setText(timeString);
        }
    }

    private void updateWeatherEffects(WeatherState weather) {
        if (weather == null || particleSystem == null) return;

        if (currentWeatherEffect != null) {
            // Stop current effects by calling their internal stop method
            // to allow the effect to clean up properly
            currentWeatherEffect.stop(particleSystem);
        } else {
            // Hard reset (just in case)
            particleSystem.stopAnimation();
        }

        // Start new effect if one exists for this weather
        ParticleEffect newWeatherEffect = weather.getParticleEffect();

        if (newWeatherEffect != null) {
            newWeatherEffect.start(particleSystem);
        }

        currentWeatherEffect = newWeatherEffect;
    }

    public StackPane getTvClickArea() {
        return tvClickArea;
    }
}