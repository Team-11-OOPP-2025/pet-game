import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FactoryTest {
    final WeatherSystem weather = new WeatherSystem();
    final GameClock clock = new GameClock();

    @Test
    void testFactoryCreatesValidPet() {
        PetModel pet = PetFactory.createNewPet("TestPet", weather, clock);
        assertNotNull(pet);
    }

    @Test
    void testDependenciesInjected() {
        PetModel pet = PetFactory.createNewPet("TestDep", weather, clock);
        assertNotNull(pet.getWeatherSystem());
        assertNotNull(pet.getInventory());
    }
}
