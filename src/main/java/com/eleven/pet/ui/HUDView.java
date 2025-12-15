package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.environment.time.GameClock;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lombok.Getter;

import static com.eleven.pet.ui.ViewConstants.*;

/**
 * Heads-up display overlay for the pet screen.
 * <p>
 * Shows pet stats (happiness, hunger, energy, cleanliness), the main clock,
 * and action buttons (feed, clean, sleep, play) bound to {@link PetController}.
 */
public class HUDView extends StackPane {

    // Layout Constants
    private static final double BAR_HEIGHT_LARGE = 38;
    private static final double BAR_HEIGHT_SMALL = 25;
    private static final double BAR_WIDTH_LARGE = 225;
    private static final double BAR_WIDTH_SMALL = 150;

    // Position of the Stats Container (Top-Left)
    private static final Insets MARGIN_STATS_BOX = new Insets(90, 0, 0, 20);
    private static final Insets MARGIN_CLOCK = new Insets(20, 0, 0, 0);

    // Button Positions
    private static final Insets MARGIN_BTN_FEED = new Insets(0, 0, 90, 20);
    private static final Insets MARGIN_BTN_CLEAN = new Insets(0, 0, 90, 150);
    private static final Insets MARGIN_BTN_SLEEP = new Insets(0, 0, 150, 20);
    private static final Insets MARGIN_BTN_PLAY = new Insets(0, 20, 90, 0);

    private final PetModel model;
    private final PetController controller;
    private final GameClock clock;

    private Label timeLabel;
    private Rectangle hungerFill;
    private Rectangle energyFill;
    private Rectangle cleanFill;
    private Rectangle happinessFill;

    // Get these for the Tutorials
    @Getter
    private VBox statsBox;
    @Getter
    private Button feedBtn;
    @Getter
    private Button sleepBtn;

    /**
     * Creates a new HUD view for the given pet and clock.
     *
     * @param model      pet model providing stat values and current state
     * @param controller controller invoked by user actions on HUD buttons
     * @param clock      game clock used to update the displayed time
     */
    public HUDView(PetModel model, PetController controller, GameClock clock) {
        this.model = model;
        this.controller = controller;
        this.clock = clock;

        setPickOnBounds(false);

        setupHUDLayer();
        setupControlLayer();
        bindData();
        observeEnvironment();
    }

    /**
     * Builds the static HUD layer containing the stat bars and clock widget
     * and adds it to this {@link StackPane}.
     */
    private void setupHUDLayer() {
        // Create the Bars
        StackPane happyBar = createStatBar(null, "😃", COLOR_HAPPINESS, BAR_WIDTH_LARGE, BAR_HEIGHT_LARGE);
        happinessFill = (Rectangle) happyBar.getChildren().get(1);

        StackPane hungerBar = createStatBar(null, "🍖 ", COLOR_HUNGER, BAR_WIDTH_SMALL, BAR_HEIGHT_SMALL);
        hungerFill = (Rectangle) hungerBar.getChildren().get(1);

        StackPane energyBar = createStatBar(null, "⚡️ ", COLOR_ENERGY, BAR_WIDTH_SMALL, BAR_HEIGHT_SMALL);
        energyFill = (Rectangle) energyBar.getChildren().get(1);

        StackPane cleanBar = createStatBar(null, "🧽 ", COLOR_CLEANLINESS, BAR_WIDTH_SMALL, BAR_HEIGHT_SMALL);
        cleanFill = (Rectangle) cleanBar.getChildren().get(1);

        // Stack them vertically using VBox
        statsBox = new VBox(STATS_BOX_SPACING); // Use consistent spacing (20px)
        statsBox.getChildren().addAll(happyBar, hungerBar, energyBar, cleanBar);
        statsBox.setMaxSize(BAR_WIDTH_LARGE, VBox.USE_PREF_SIZE); // Clamp size to content

        // Add VBox to Layout (So the bars move together)
        addToLayout(statsBox, Pos.TOP_LEFT, MARGIN_STATS_BOX);

        // Clock
        timeLabel = createClockWidget();
        addToLayout(timeLabel, Pos.TOP_CENTER, MARGIN_CLOCK);
    }

    /**
     * Builds the interactive control layer (feed, clean, sleep, play buttons)
     * and binds them to the {@link PetController}.
     */
    private void setupControlLayer() {
        feedBtn = createActionButton("FEED", PIXEL_BUTTON_PRIMARY, PIXEL_BUTTON_WIDTH, () -> controller.setInventoryOpen(true));
        addToLayout(feedBtn, Pos.BOTTOM_LEFT, MARGIN_BTN_FEED);

        Button cleanBtn = createActionButton("CLEAN", PIXEL_BUTTON_PRIMARY, PIXEL_BUTTON_WIDTH, controller::handleCleanAction);
        addToLayout(cleanBtn, Pos.BOTTOM_LEFT, MARGIN_BTN_CLEAN);

        sleepBtn = createActionButton("SLEEP", PIXEL_BUTTON_SLEEP, PIXEL_BUTTON_WIDTH, controller::handleSleepAction);
        sleepBtn.setVisible(false);
        addToLayout(sleepBtn, Pos.BOTTOM_LEFT, MARGIN_BTN_SLEEP);

        Button playBtn = createActionButton("PLAY", PIXEL_BUTTON_PRIMARY, PIXEL_BUTTON_WIDTH, controller::handlePlayAction);
        addToLayout(playBtn, Pos.BOTTOM_RIGHT, MARGIN_BTN_PLAY);
    }

    /**
     * Creates a single stat bar with icon and optional label.
     *
     * @param label  optional text label (may be {@code null})
     * @param icon   icon text (e.g. emoji) displayed on the bar
     * @param color  fill color of the bar
     * @param width  preferred bar width in pixels
     * @param height preferred bar height in pixels
     * @return configured {@link StackPane} representing the bar
     */
    private StackPane createStatBar(String label, String icon, Color color, double width, double height) {
        StackPane container = new StackPane();
        container.setMinSize(width, height);
        container.setMaxSize(width, height);

        // Background Track
        Rectangle track = new Rectangle(width, height, Color.web("#ecf0f1"));
        track.setArcWidth(10);
        track.setArcHeight(10);
        track.setStroke(Color.BLACK);
        track.setStrokeWidth(2);

        // Fill Bar
        Rectangle fill = new Rectangle(0, height, color);
        fill.setArcWidth(10);
        fill.setArcHeight(10);
        StackPane.setAlignment(fill, Pos.CENTER_LEFT);

        // Text Overlay using HBox
        HBox textContainer = new HBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        textContainer.setPadding(new Insets(0, 0, 0, 10));

        Text iconText = new Text(icon);
        iconText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 12));

        if (label == null) label = "";
        Text labelText = new Text(label);
        labelText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, label.equals("Happiness") ? 16 : 14));

        textContainer.getChildren().addAll(iconText, labelText);

        container.getChildren().addAll(track, fill, textContainer);
        return container;
    }

    /**
     * Creates a styled pixel-art action button and wires it to the given action.
     *
     * @param text     button label
     * @param cssClass CSS class to apply in addition to the default pixel style
     * @param width    preferred button width
     * @param action   action to invoke when the button is pressed
     * @return configured {@link Button}
     */
    private Button createActionButton(String text, String cssClass, double width, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(width);
        btn.getStyleClass().addAll(PIXEL_BUTTON_STYLE_CLASS, cssClass);

        if (controller != null) {
            btn.setOnAction(_ -> action.run());
        }
        return btn;
    }

    /**
     * Creates the main clock label used to display in-game time.
     *
     * @return configured {@link Label} instance
     */
    private Label createClockWidget() {
        Label lbl = new Label("00:00");
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 36));
        lbl.setTextFill(Color.WHITE);
        lbl.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10; -fx-padding: 10 20;");
        return lbl;
    }

    /**
     * Adds a node to this HUD with the given alignment and margin.
     *
     * @param node   node to add
     * @param pos    alignment within the {@link StackPane}
     * @param margin margin around the node
     */
    private void addToLayout(javafx.scene.Node node, Pos pos, Insets margin) {
        StackPane.setAlignment(node, pos);
        StackPane.setMargin(node, margin);
        getChildren().add(node);
    }

    /**
     * Binds pet stats, pet state, and other observable data from the model
     * to the HUD's visual components.
     */
    private void bindData() {
        bindBar(model.getStatProperty(PetStats.STAT_HUNGER), hungerFill, BAR_WIDTH_SMALL);
        bindBar(model.getStatProperty(PetStats.STAT_ENERGY), energyFill, BAR_WIDTH_SMALL);
        bindBar(model.getStatProperty(PetStats.STAT_CLEANLINESS), cleanFill, BAR_WIDTH_SMALL);

        var happyStat = model.getStatProperty(PetStats.STAT_HAPPINESS);
        happyStat.addListener((_, _, val) -> updateFill(happinessFill, val.intValue(), BAR_WIDTH_LARGE));
        updateFill(happinessFill, happyStat.get(), BAR_WIDTH_LARGE);

        model.getStateProperty().addListener((_, _, state) -> refreshSleepButton(state));
    }

    /**
     * Updates sleep button enablement based on the current {@link PetState}.
     *
     * @param state current pet state
     */
    private void refreshSleepButton(PetState state) {
        toggleSleepButton(state instanceof AsleepState);
    }

    /**
     * Binds a numeric stat property (0–100) to a bar rectangle width.
     *
     * @param stat observable stat value
     * @param fill rectangle representing the bar fill
     * @param maxW maximum width when stat is 100
     */
    private void bindBar(javafx.beans.value.ObservableValue<Number> stat, Rectangle fill, double maxW) {
        if (stat != null) {
            stat.addListener((_, _, val) -> updateFill(fill, val.intValue(), maxW));
            updateFill(fill, stat.getValue().intValue(), maxW);
        }
    }

    /**
     * Updates the width of a stat bar based on a percentage value.
     *
     * @param rect     bar rectangle
     * @param value    stat value in range 0–100
     * @param maxWidth width corresponding to value 100
     */
    private void updateFill(Rectangle rect, int value, double maxWidth) {
        rect.setWidth(maxWidth * (value / 100.0));
    }

    /**
     * Subscribes to game clock changes and updates the clock label
     * and sleep button visibility accordingly.
     */
    private void observeEnvironment() {
        if (clock == null) return;

        clock.gameTimeProperty().addListener((_, _, time) -> {
            double t = time.doubleValue();
            updateClockLabel(t);
            boolean canSleep = controller.isSleepAllowed();
            sleepBtn.setVisible(canSleep);
        });

        updateClockLabel(clock.getGameTime());
    }

    /**
     * Formats and displays the given in-game time on the clock label.
     *
     * @param time in-game time in hours, where the integer part is hours and
     *             the fractional part represents minutes (e.g. 13.5 = 13:30)
     */
    private void updateClockLabel(double time) {
        int hours = (int) time % 24;
        int minutes = (int) ((time % 1.0) * 60);
        String timeString = String.format("%02d:%02d", hours, minutes);
        timeLabel.setText(timeString);
    }

    /**
     * Enables or disables the sleep button based on whether the pet is sleeping.
     *
     * @param isSleeping {@code true} if pet is currently asleep
     */
    private void toggleSleepButton(boolean isSleeping) {
        sleepBtn.setDisable(isSleeping);
        sleepBtn.setOpacity(isSleeping ? 0.5 : 1.0);
    }
}