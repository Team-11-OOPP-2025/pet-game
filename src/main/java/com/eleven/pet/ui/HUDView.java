package com.eleven.pet.ui;

import com.eleven.pet.character.PetController;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.inventory.ui.InventoryView;
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

import static com.eleven.pet.ui.ViewConstants.*;

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
    private final InventoryView inventoryView;
    private final GameClock clock;

    private Label timeLabel;
    private Rectangle hungerFill;
    private Rectangle energyFill;
    private Rectangle cleanFill;
    private Rectangle happinessFill;
    private StackPane sleepBtnContainer;

    public HUDView(PetModel model, PetController controller, InventoryView inventoryView, GameClock clock) {
        this.model = model;
        this.controller = controller;
        this.inventoryView = inventoryView;
        this.clock = clock;

        setPickOnBounds(false);

        setupHUDLayer();
        setupControlLayer();
        bindData();
        observeEnvironment();
    }

    private void setupHUDLayer() {
        // TODO: Decide if we want labels on the bars
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
        VBox statsBox = new VBox(STATS_BOX_SPACING); // Use consistent spacing (20px)
        statsBox.getChildren().addAll(happyBar, hungerBar, energyBar, cleanBar);
        statsBox.setMaxSize(BAR_WIDTH_LARGE, VBox.USE_PREF_SIZE); // Clamp size to content

        // Add VBox to Layout (Only ONE margin calculation needed now!)
        addToLayout(statsBox, Pos.TOP_LEFT, MARGIN_STATS_BOX);

        // Clock
        timeLabel = createClockWidget();
        addToLayout(timeLabel, Pos.TOP_CENTER, MARGIN_CLOCK);
    }

    private void setupControlLayer() {
        StackPane feedBtnContainer = createActionButton("FEED", COLOR_BTN_PRIMARY, 120, () -> inventoryView.toggleInventory(true));
        addToLayout(feedBtnContainer, Pos.BOTTOM_LEFT, MARGIN_BTN_FEED);

        StackPane cleanBtnContainer = createActionButton("CLEAN", COLOR_BTN_PRIMARY, 120, controller::handleCleanAction);
        addToLayout(cleanBtnContainer, Pos.BOTTOM_LEFT, MARGIN_BTN_CLEAN);

        sleepBtnContainer = createActionButton("SLEEP", COLOR_BTN_SLEEP, 120, controller::handleSleepAction);
        ((Button) sleepBtnContainer.getChildren().get(1)).setTextFill(COLOR_BTN_TEXT_LIGHT);
        sleepBtnContainer.setVisible(false);
        addToLayout(sleepBtnContainer, Pos.BOTTOM_LEFT, MARGIN_BTN_SLEEP);

        StackPane playBtnContainer = createActionButton("PLAY", COLOR_BTN_PRIMARY, 140, controller::handlePlayAction);
        addToLayout(playBtnContainer, Pos.BOTTOM_RIGHT, MARGIN_BTN_PLAY);
    }

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

    private StackPane createActionButton(String text, Color bg, double width, Runnable action) {
        StackPane container = new StackPane();
        container.setMaxSize(width, 50);

        Rectangle border = new Rectangle(width, 50, bg);
        border.setStroke(Color.BLACK);
        border.setStrokeWidth(3);

        Button btn = new Button(text);
        btn.setPrefSize(width, 50);
        btn.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        btn.setTextFill(bg.equals(Color.WHITE) ? COLOR_BTN_TEXT_DARK : COLOR_BTN_TEXT_LIGHT);
        btn.setStyle("-fx-background-color: transparent;");

        if (controller != null) {
            btn.setOnAction(_ -> action.run());
        }

        container.getChildren().addAll(border, btn);
        return container;
    }

    private Label createClockWidget() {
        Label lbl = new Label("00:00");
        lbl.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 36));
        lbl.setTextFill(Color.WHITE);
        lbl.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 10; -fx-padding: 10 20;");
        return lbl;
    }

    private void addToLayout(javafx.scene.Node node, Pos pos, Insets margin) {
        StackPane.setAlignment(node, pos);
        StackPane.setMargin(node, margin);
        getChildren().add(node);
    }

    private void bindData() {
        if (model == null) return;

        PetStats stats = model.getStats();
        if (stats != null) {
            bindBar(stats.getStat(PetStats.STAT_HUNGER), hungerFill, BAR_WIDTH_SMALL);
            bindBar(stats.getStat(PetStats.STAT_ENERGY), energyFill, BAR_WIDTH_SMALL);
            bindBar(stats.getStat(PetStats.STAT_CLEANLINESS), cleanFill, BAR_WIDTH_SMALL);

            var happyStat = stats.getStat(PetStats.STAT_HAPPINESS);
            if (happyStat != null) {
                happyStat.addListener((_, _, val) -> updateFill(happinessFill, val.intValue(), BAR_WIDTH_LARGE));
                updateFill(happinessFill, happyStat.get(), BAR_WIDTH_LARGE);
            }
        }

        model.getStateProperty().addListener((_, _, state) -> refreshSleepButton(state));
    }

    private void refreshSleepButton(PetState state) {
        toggleSleepButton(state instanceof AsleepState);
    }

    private void bindBar(javafx.beans.value.ObservableValue<Number> stat, Rectangle fill, double maxW) {
        if (stat != null) {
            stat.addListener((_, _, val) -> updateFill(fill, val.intValue(), maxW));
            updateFill(fill, stat.getValue().intValue(), maxW);
        }
    }

    private void updateFill(Rectangle rect, int value, double maxWidth) {
        rect.setWidth(maxWidth * (value / 100.0));
    }

    private void observeEnvironment() {
        if (clock == null) return;

        clock.gameTimeProperty().addListener((_, _, time) -> {
            double t = time.doubleValue();
            updateClockLabel(t);
            boolean canSleep = controller.isSleepAllowed();
            sleepBtnContainer.setVisible(canSleep);
        });

        updateClockLabel(clock.getGameTime());
    }

    private void updateClockLabel(double time) {
        int hours = (int) time % 24;
        int minutes = (int) ((time % 1.0) * 60);
        String timeString = String.format("%02d:%02d", hours, minutes);
        timeLabel.setText(timeString);
    }

    private void toggleSleepButton(boolean isSleeping) {
        sleepBtnContainer.setDisable(isSleeping);
        sleepBtnContainer.setOpacity(isSleeping ? 0.5 : 1.0);
    }
}