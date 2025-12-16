package com.eleven.pet.ui;

import javafx.scene.paint.Color;

/**
 * Centralized constants for UI layout, dimensions, colors and styling.
 * <p>
 * All view classes should reference these constants instead of hard-coding
 * pixel values or color literals to keep the UI consistent.
 */
public class ViewConstants {
    // Reference Resolution (The design resolution of the background art)
    public static final double REF_WIDTH = 624.0;
    public static final double REF_HEIGHT = 351.0;


    // Fonts
    public static final String FONT_FAMILY = "Minecraft";

    // Colors
    public static final Color COLOR_HAPPINESS = Color.web("#f4d03f");
    public static final Color COLOR_HUNGER = Color.web("#2ecc71");
    public static final Color COLOR_ENERGY = Color.web("#f39c12");
    public static final Color COLOR_CLEANLINESS = Color.web("#3498db");

    public static final Color COLOR_BTN_PRIMARY = Color.WHITE;
    public static final Color COLOR_BTN_SLEEP = Color.web("#3498db");
    public static final Color COLOR_BTN_TEXT_DARK = Color.BLACK;
    public static final Color COLOR_BTN_TEXT_LIGHT = Color.WHITE;

    public static final String PIXEL_BUTTON_PRIMARY = "pixel-btn-primary";
    public static final String PIXEL_BUTTON_SLEEP = "pixel-btn-sleep";
    public static final String PIXEL_BUTTON_GOLD = "pixel-btn-gold";
    public static final Integer PIXEL_BUTTON_WIDTH = 120;
    
    // Styling
    public static final String STYLE_INVENTORY_PANEL =
            "-fx-background-color: #fdf5e6; " +
                    "-fx-background-radius: 15; " +
                    "-fx-border-color: #8b4513; " +
                    "-fx-border-width: 4; " +
                    "-fx-border-radius: 10; " +
                    "-fx-padding: 15; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 5);";

    public static final String STYLE_ITEM_SLOT =
            "-fx-background-color: white; " +
                    "-fx-border-color: #d2b48c; " +
                    "-fx-border-width: 2; " +
                    "-fx-border-radius: 5; " +
                    "-fx-background-radius: 5;";

    public static final String STYLE_CLOSE_BTN =
            "-fx-background-color: #8b4513; -fx-text-fill: white; -fx-background-radius: 20;";

    public static final String STYLE_CONTENT_BOX =
            "-fx-background-color: #fdf5e6;" +
                    "-fx-border-color: #8b4513;" +
                    "-fx-border-width: 4;" +
                    "-fx-background-radius: 10;" +
                    "-fx-border-radius: 10;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);";

    // Layout Spacing
    public static final double STATS_BOX_SPACING = 20.0;

    public static final String PIXEL_BUTTON_STYLE_CLASS = "pixel-btn";
}