import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.FoodItem;
import com.eleven.pet.view.AssetLoader;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
