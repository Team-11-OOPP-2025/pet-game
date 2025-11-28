import com.eleven.pet.view.AssetLoader;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class AssetLoaderTest {
    @Test
    public void testAssetLoading() {
        AssetLoader assetLoader = AssetLoader.getInstance();
        String imageName = "Bear";
        Image image = assetLoader.getImage(imageName);
        assertEquals(311.0, image.getWidth(), 0.1);
        assertEquals(461.0, image.getHeight(), 0.1);
    }

    @Test
    public void testSingleton() {
        AssetLoader assetLoader = AssetLoader.getInstance();
        AssetLoader anotherInstance = AssetLoader.getInstance();
        assertSame(assetLoader, anotherInstance);
    }

    @Test
    public void testMissingImageFallback() {
        AssetLoader assetLoader = AssetLoader.getInstance();
        String missingImagePath = "images/non_existent_image.png";
        Image image = assetLoader.getImage(missingImagePath);
        assertSame(null, image);
    }
}
