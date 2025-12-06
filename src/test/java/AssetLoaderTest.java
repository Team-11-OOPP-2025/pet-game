import com.eleven.pet.view.AssetLoader;
import javafx.scene.image.Image;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AssetLoaderTest {
    private AssetLoader assetLoader;

    @BeforeEach
    void setUp() {
        assetLoader = AssetLoader.getInstance();
        assetLoader.loadAll();
    }

    @Test
    void testAssetLoading() {
        String imageName = "Bear";
        Image image = assetLoader.getImage(imageName);
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
        Image image = assetLoader.getImage(missingImagePath);
        assertSame(null, image);
    }
}
