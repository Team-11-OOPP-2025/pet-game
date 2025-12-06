import com.eleven.pet.core.AssetLoader;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AssetLoaderTest {
    private AssetLoader assetLoader;

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.loadAll();
    }

    @Test
    void testAssetLoading() {
        String imagePath = "pet/idle/Bear";
        Image image = assetLoader.getImage(imagePath);
        assertEquals(311.0, image.getWidth(), 0.1);
        assertEquals(461.0, image.getHeight(), 0.1);
    }

    @Test
    void testSingleton() {
        assertSame(AssetLoader.getInstance(), assetLoader);
    }

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
