import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for {@link PetFactory} creation and dependency wiring.
 */
public class FactoryTest {
    final WeatherSystem weather = new WeatherSystem();
    final GameClock clock = new GameClock();

    /**
     * Verifies that {@link PetFactory#createNewPet(String, WeatherSystem, GameClock)}
     * returns a non-null {@link PetModel}.
     */
    @Test
    void testFactoryCreatesValidPet() {
        PetModel pet = PetFactory.createNewPet("TestPet", weather, clock);
        assertNotNull(pet);
    }

    /**
     * Ensures that key dependencies, such as {@link WeatherSystem} and the inventory,
     * are properly injected into the created {@link PetModel}.
     */
    @Test
    void testDependenciesInjected() {
        PetModel pet = PetFactory.createNewPet("TestDep", weather, clock);
        assertNotNull(pet.getWeatherSystem());
        assertNotNull(pet.getInventory());
    }
}
