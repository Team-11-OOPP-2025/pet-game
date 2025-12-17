package com.eleven.pet.core;

import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link AssetLoader} singleton and asset retrieval behavior.
 */
public class AssetLoaderTest {
    private AssetLoader assetLoader;

    /**
     * Initializes the shared {@link AssetLoader} instance and loads all assets
     * before each test.
     */
    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.loadAll();
    }

    /**
     * Verifies that a known image asset is loaded with the expected dimensions.
     */
    @Test
    void testAssetLoading() {
        String imagePath = "backgrounds/DAY";
        Image image = assetLoader.getImage(imagePath);
        assertEquals(624.0, image.getWidth(), 0.1);
        assertEquals(351.0, image.getHeight(), 0.1);
    }

    /**
     * Ensures that {@link AssetLoader} behaves as a singleton.
     */
    @Test
    void testSingleton() {
        assertSame(AssetLoader.getInstance(), assetLoader);
    }

    /**
     * Verifies that requesting a missing image returns a non-null placeholder
     * with the expected dimensions.
     */
    @Test
    void testMissingImageFallback() {
        AssetLoader assetLoader = AssetLoader.getInstance();
        String missingImagePath = "non_existent_image";

        // Attempt to load a missing image
        Image image = assetLoader.getImage(missingImagePath);

        // 1. Verify it didn't crash and didn't return null
        assertNotNull(image, "Loader should return a placeholder for missing assets");

        // 2. Verify it is actually the placeholder (Check dimensions based on your createPlaceholderImage logic)
        assertEquals(64.0, image.getWidth(), "Placeholder width should be 64");
        assertEquals(64.0, image.getHeight(), "Placeholder height should be 64");
    }
}
