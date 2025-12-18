package com.eleven.pet.core;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip; // Modern JavaFX audio class

import java.io.InputStream;
import java.net.URL; // Standard networking URL
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * AssetLoader is responsible for loading and caching image assets and fonts.
 *
 * <p>Images are loaded from the {@code /images/} classpath location and
 * cached by relative path. Missing images are replaced with a placeholder.</p>
 */
public class AssetLoader {
    private static AssetLoader instance;
    private final Map<String, Image> imageCache = new HashMap<>();
    private final Map<String, AudioClip> soundCache = new HashMap<>();
    
    // Base path for all sounds
    private static final String SOUND_ROOT = "/audio/";
    // Base path for all images
    private static final String IMAGE_ROOT = "/images/";
    // Base path for all fonts
    private static final String FONT_ROOT = "/fonts/";

    /**
     * Returns the singleton {@code AssetLoader} instance.
     *
     * @return the global {@code AssetLoader}
     */
    public static AssetLoader getInstance() {
        if (instance == null) {
            instance = new AssetLoader();
        }
        return instance;
    }


    /**
     * Loads and plays a sound effect.
     * @param relativePath the name of the sound file (without extension) inside /resources/sounds/
     */
    public void playSound(String relativePath) {
        AudioClip clip = soundCache.computeIfAbsent(relativePath, this::loadSound);
        if (clip != null) {
            // Stop if currently playing to allow rapid re-triggering or overlapping
            if (clip.isPlaying()) {
                clip.stop();
            }
            clip.play();
        }
    }

    /**
     * Loads an audio clip from disk.
     */
    private AudioClip loadSound(String relativePath) {
        // Supported audio extensions
        String[] extensions = {".wav", ".mp3"};

        for (String ext : extensions) {
            String fullPath = SOUND_ROOT + relativePath + ext;
            try {
                // Use java.net.URL to find the resource
                URL url = getClass().getResource(fullPath);
                if (url != null) {
                    return new AudioClip(url.toExternalForm());
                }
            } catch (Exception e) {
                System.err.println("Error loading sound: " + fullPath);
            }
        }
        System.err.println("Sound not found: " + relativePath);
        return null;
    }

    /**
     * Loads an image from the cache or disk.
     *
     * @param relativePath the path relative to {@code /resources/images/}
     *                     (e.g., {@code "backgrounds/Day"} without extension)
     * @return the loaded image or a placeholder if not found
     */
    public Image getImage(String relativePath) {
        return imageCache.computeIfAbsent(relativePath, this::loadImage);
    }

    /**
     * Loads a set of standard icon sizes based on a base path.
     * Expects files named: basePath_16.png, basePath_32.png, etc.
     */
    public List<Image> getIcons(String basePath) {
        List<Image> images = new ArrayList<>();

        // The standard sizes you listed
        int[] sizes = {16, 32, 48, 64, 128, 256};

        for (int size : sizes) {
            // Construct the path: "icons/bjorni_16"
            String fullPath = basePath + "_" + size;

            // if one specific size is missing
            try {
                Image img = getImage(fullPath);
                if (img != null && !img.isError()) {
                    images.add(img);
                }
            } catch (Exception e) {
                System.err.println("Could not load icon size: " + size);
            }
        }

        return images;
    }

    /**
     * Loads a font from disk and registers it with JavaFX.
     *
     * @param fontName the font file name (e.g., {@code "Minecraft.ttf"})
     * @param size     the font size in points
     */
    private void loadFont(String fontName, double size) {
        String fontPath = FONT_ROOT + fontName;
        try (InputStream stream = getClass().getResourceAsStream(fontPath)) {
            if (stream != null) {
                javafx.scene.text.Font.loadFont(stream, size);
            } else {
                System.err.println("ERROR: Font not found: " + fontPath);
            }
        } catch (Exception e) {
            System.err.println("Error loading font: " + fontPath);
            e.printStackTrace();
        }
    }

    /**
     * Loads an image from disk, trying multiple file extensions.
     *
     * <p>Extensions are tried in order: {@code .png}, {@code .jpg}, {@code .jpeg}.
     * If no variant is found, a placeholder image is returned.</p>
     *
     * @param relativePath the path relative to {@code /resources/images/} without extension
     * @return the loaded {@link Image}, or a placeholder if not found
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
     * @return placeholder image used when real assets are missing
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
     *
     * <p>This is typically called once at application startup to reduce
     * stuttering when assets are first requested.
     */
    public void loadAll() {
        System.out.println("Preloading assets...");

        loadFont("Minecraft.ttf", 14);
        String[] assets = {
                "backgrounds/DAWN",
                "backgrounds/MORNING",
                "backgrounds/DAY",
                "backgrounds/EVENING",
                "backgrounds/EARLY_NIGHT",
                "backgrounds/DEEP_NIGHT",

                "sprites/SpriteSheetNeutral",
                "sprites/SpriteSheetHappy",
                "sprites/SpriteSheetSleeping",
                "sprites/SpriteSheetSad",
                "sprites/SpriteSheetCrying"
        };

        for (String asset : assets) {
            getImage(asset);
        }
        System.out.println("Assets loaded.");
    }
}