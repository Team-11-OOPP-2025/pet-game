import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.items.FoodItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FactoryTest {
    WeatherSystem weather = new WeatherSystem();
    GameClock clock = new GameClock();

    @Test
    void testFactoryCreatesValidPet() {
        PetModel pet = PetFactory.createNewPet("TestPet", weather, clock);
        assertNotNull(pet);
    }

    @Test
    void testDefaultInventory() {
        PetModel pet = PetFactory.createNewPet("TestInventory", weather, clock);
        FoodItem food = new FoodItem("Food", 30);
        assertEquals(1, pet.getInventory().getQuantity(food));
    }

    @Test
    void testDependenciesInjected() {
        PetModel pet = PetFactory.createNewPet("TestDep", weather, clock);
        assertNotNull(pet.getWeatherSystem());
        assertNotNull(pet.getInventory());
    }
}
