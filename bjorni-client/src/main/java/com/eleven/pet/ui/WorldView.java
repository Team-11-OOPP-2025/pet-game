package com.eleven.pet.ui;

import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.core.GameConfig;
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

/**
 * Main world background view.
 * <p>
 * Renders the room background, window particle effects, TV interaction area,
 * and a background clock synchronized with {@link GameClock} and {@link WeatherSystem}.
 * </p>
 */
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

    private static final double CLOCK_X = 455;
    private static final double CLOCK_Y = 30;
    private static final double CLOCK_WIDTH = 200;
    private static final double CLOCK_HEIGHT = 100;

    /**
     * Game-time clock used to drive background day/night cycle and digital clock display.
     */
    private final GameClock clock;

    /**
     * Shared asset loader for resolving images such as backgrounds and UI frames.
     */
    private final AssetLoader assetLoader;

    /**
     * Particle system responsible for rendering window weather effects.
     */
    public final ParticleSystem particleSystem;

    /**
     * Weather system that defines current weather and associated visual effects.
     */
    private final WeatherSystem weatherSystem;

    /**
     * Image view used to render the room background (day/night variants).
     */
    private ImageView backgroundView;

    /**
     * Container for the decorative background clock (frame + label).
     */
    private StackPane backgroundClockPane;

    /**
     * Label displaying the in-game time in HH:mm format on the background clock.
     */
    private Label backgroundClockLabel;

    /**
     * Invisible, clickable TV overlay area for attaching interaction handlers.
     */
    private StackPane tvClickArea;

    /**
     * Pane inside the TV area used exclusively for dynamic content (minigames).
     */
    private StackPane tvContentPane;

    /**
     * Default daytime background image, used as a fallback when no clock is present.
     */
    private Image backgroundDay;

    /**
     * Currently active weather particle effect; may be {@code null} if none is playing.
     */
    private ParticleEffect currentWeatherEffect;

    /**
     * Constructs a new {@code WorldView}.
     * <p>
     * Initializes the background, particle effects, TV interaction area, and clock.
     *
     * @param clock         the {@link GameClock} instance to synchronize the clock and background;
     *                      may be {@code null} to render a static daytime scene
     * @param weatherSystem the {@link WeatherSystem} instance to render weather effects
     */
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

    /**
     * Loads all static image assets required by this view (e.g. daytime background).
     */
    private void loadAssets() {
        backgroundDay = assetLoader.getImage("backgrounds/DAY");
    }

    /**
     * Sets up the scalable room background layer and binds it to the view size.
     * <p>
     * If a {@link GameClock} is present, the background is immediately updated
     * to match the current {@link DayCycle}.
     */
    private void setupBackgroundLayer() {
        backgroundView = new ImageView();
        backgroundView.setPreserveRatio(false);
        backgroundView.fitWidthProperty().bind(widthProperty());
        backgroundView.fitHeightProperty().bind(heightProperty());

        if (clock != null) updateBackground(clock.getCycle());
        else backgroundView.setImage(backgroundDay);

        getChildren().add(backgroundView);
    }

    /**
     * Initializes the particle layer for window weather effects and binds it
     * to the reference window area using relative coordinates.
     */
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

    /**
     * Creates the invisible TV interaction overlay and binds its position and size
     * to the reference TV area. The caller can obtain the overlay via {@link #getTvContentPane()}.
     */
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

        // Centered decorative image inside the TV area (clicks still go to the overlay)
        Image tvCenterImage = assetLoader.getImage("ui/game-controller");
        if (tvCenterImage != null) {
            ImageView tvImageView = new ImageView(tvCenterImage);
            tvImageView.setPreserveRatio(true);
            tvImageView.setFitWidth(200);
            tvImageView.setMouseTransparent(true);
            tvClickArea.getChildren().add(tvImageView);
        }

        tvContentPane = new StackPane();
        tvContentPane.setPickOnBounds(false);
        tvContentPane.maxWidthProperty().bind(tvClickArea.widthProperty());
        tvContentPane.maxHeightProperty().bind(tvClickArea.heightProperty());
        tvClickArea.getChildren().add(tvContentPane);

        tvOverlay.getChildren().add(tvClickArea);
        getChildren().add(tvOverlay);
    }

    /**
     * Creates and positions the decorative background digital clock, including
     * its frame and time label, and binds it to the reference coordinates.
     */
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
        backgroundClockLabel.setFont(Font.font(FONT_FAMILY, FontWeight.MEDIUM, 40));
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

    /**
     * Subscribes this view to environment changes such as time-of-day and weather.
     * <p>
     * Updates the background image, clock label, and weather particle effects
     * whenever the corresponding properties change.
     */
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

    /**
     * Updates the room background image based on the current {@link DayCycle}.
     *
     * @param cycle the day cycle state determining which background to use
     */
    private void updateBackground(DayCycle cycle) {
        Image bg = assetLoader.getImage("backgrounds/" + cycle.name());
        if (bg != null) backgroundView.setImage(bg);
    }

    /**
     * Updates the background digital clock label to reflect the current in-game time.
     * <p>
     * The time is displayed in 24-hour {@code HH:mm} format. Minutes are currently
     * fixed to {@code 00}, as sub-hour precision is not yet represented visually.
     * </p>
     *
     * @param time the in-game time in hours, where the integer part represents
     *             the hour of day in range {@code [0, 24)}
     */
    private void updateClockLabel(double time) {
        // 0.0 -> 0:00, GameConfig.DAY_LENGTH_SECONDS -> 24:00 (wraps to 0:00)
        double dayFraction = time / GameConfig.DAY_LENGTH_SECONDS; // 0.0–1.0

        double totalHours = dayFraction * 24.0;   // 0.0–24.0
        int hours = (int) totalHours;            // 0–23
        int minutes = (int) ((totalHours - hours) * 60.0); // 0–59

        String timeString = String.format("%02d:%02d", hours, minutes);
        backgroundClockLabel.setText(timeString);
    }


    /**
     * Starts or stops weather particle effects according to the provided state.
     * <p>
     * Any existing effect is cleanly stopped before the new one is started. If
     * {@code weather} has no associated {@link ParticleEffect}, all effects are stopped.
     *
     * @param weather the new {@link WeatherState} to visualize; may be {@code null}
     */
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

    /**
     * Returns the clickable TV area overlay.
     * <p>
     * The caller can attach mouse handlers to trigger minigames or other actions.
     *
     * @return the {@link StackPane} representing the TV interaction area
     */
    public StackPane getTvClickArea() {
        return tvClickArea;
    }

    /**
     * Returns the pane used for dynamic TV content (minigames).
     */
    public StackPane getTvContentPane() {
        return tvContentPane;
    }
}