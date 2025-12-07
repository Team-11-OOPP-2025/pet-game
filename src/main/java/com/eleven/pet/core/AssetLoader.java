package com.eleven.pet.core;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * AssetLoader is responsible for loading and caching image assets.
 * It supports loading images from the /resources/images/ directory
 * and handles missing assets by providing a placeholder image.
 */
public class AssetLoader {
    private static AssetLoader instance;
    private final Map<String, Image> imageCache = new HashMap<>();

    // Base path for all images
    private static final String IMAGE_ROOT = "/images/";

    /**
     * Private constructor for singleton pattern.
     */
    public static AssetLoader getInstance() {
        if (instance == null) {
            instance = new AssetLoader();
        }
        return instance;
    }

    /**
     * Loads an image from the cache or disk.
     *
     * @param relativePath The path relative to /resources/images/ (e.g., "backgrounds/Day")
     *                     You do NOT need to add .png extension.
     */
    public Image getImage(String relativePath) {
        return imageCache.computeIfAbsent(relativePath, this::loadImage);
    }

    /**
     * Loads an image from disk, trying multiple extensions.
     *
     * @param relativePath The path relative to /resources/images/
     * @return The loaded Image, or a placeholder if not found
     */
    private Image loadImage(String relativePath) {
        // Try PNG first, then JPG
        String[] extensions = {".png", ".jpg", ".jpeg"};

        for (String ext : extensions) {
            String fullPath = IMAGE_ROOT + relativePath + ext;
            try (InputStream stream = getClass().getResourceAsStream(fullPath)) {
                if (stream != null) {
                    return new Image(stream);
                }
            } catch (Exception e) {
                System.err.println("Error loading image: " + fullPath);
                e.printStackTrace();
            }
        }

        System.err.println("ERROR: Asset not found: " + relativePath + " (Checked " + IMAGE_ROOT + relativePath + ".[png|jpg])");
        return createPlaceholderImage();
    }

    /**
     * Creates a simple placeholder image (magenta square).
     *
     * @return Placeholder Image
     */
    private Image createPlaceholderImage() {
        WritableImage placeholder = new WritableImage(64, 64);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                placeholder.getPixelWriter().setColor(x, y, Color.MAGENTA);
            }
        }
        return placeholder;
    }

    /**
     * Preloads all essential assets into the cache.
     */
    public void loadAll() {
        System.out.println("Preloading assets...");
        String[] assets = {
                "backgrounds/Dawn",
                "backgrounds/Morning",
                "backgrounds/Day",
                "backgrounds/Evening",
                "backgrounds/EARLY_NIGHT",
                "backgrounds/DEEP_NIGHT",

                "sprites/idle/SpriteSheetNeutral",
                "sprites/happy/SpriteSheetHappy",
                "sprites/sleeping/SpriteSheetSleeping",
                "sprites/sad/SpriteSheetSad",
                "sprites/sad/SpriteSheetCrying"
        };

        for (String asset : assets) {
            getImage(asset);
        }
        System.out.println("Assets loaded.");
    }
}