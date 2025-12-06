package com.eleven.pet.core;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {
    private static AssetLoader instance;
    private final Map<String, Image> imageCache = new HashMap<>();

    // Base path for all images
    private static final String IMAGE_ROOT = "/images/";

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

    // Creates a bright pink square to visually warn you a texture is missing
    private Image createPlaceholderImage() {
        WritableImage placeholder = new WritableImage(64, 64);
        for (int x = 0; x < 64; x++) {
            for (int y = 0; y < 64; y++) {
                placeholder.getPixelWriter().setColor(x, y, Color.MAGENTA);
            }
        }
        return placeholder;
    }

    public void loadAll() {
        System.out.println("Preloading assets...");
        String[] assets = {
                // Backgrounds
                "backgrounds/Dawn",
                "backgrounds/Morning",
                "backgrounds/Day",
                "backgrounds/Evening",
                "backgrounds/EarlyNight",
                "backgrounds/DeepNight",

                // Pet - Idle
                "pet/idle/Bear",
                "pet/idle/LookingLeftBear",
                "pet/idle/LookingRightBear",

                // Pet - Sleeping
                "pet/sleeping/SleepingBear1",
                "pet/sleeping/SleepingBear2",

                // Pet - Happy
                "pet/happy/HappyBear1",
                "pet/happy/HappyBear2",
                "pet/happy/HappyBearLookingLeft",
                "pet/happy/HappyBearLookingRight",

                // Pet - Sad
                "pet/sad/CryingBear1",
                "pet/sad/CryingBear2",
                "pet/sad/SadBear1",
                "pet/sad/SadBear2",
        };

        for (String asset : assets) {
            getImage(asset);
        }
        System.out.println("Assets loaded.");
    }
}