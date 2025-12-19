import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link PetStats}, verifying stat registration and clamping behavior.
 */
public class PetStatsTest {

    PetStats stats;

    /**
     * Initializes a fresh {@link PetModel} and its {@link PetStats} before each test.
     */
    @BeforeEach
    public void setup() {
        PetModel petModel = PetFactory.createNewPet(null, null, null);
        stats = petModel.getStats();
    }

    /**
     * Verifies that a stat value is clamped to the defined maximum (100).
     */
    @Test
    public void testStatClampingMax() {
        stats.modifyStat(PetStats.STAT_HAPPINESS, 150);
        assertEquals(100, stats.getStat(PetStats.STAT_HAPPINESS).get());
    }

    /**
     * Verifies that a stat value is clamped to the defined minimum (0).
     */
    @Test
    public void testStatClampingMin() {
        stats.modifyStat(PetStats.STAT_HAPPINESS, -200);
        assertEquals(0, stats.getStat(PetStats.STAT_HAPPINESS).get());
    }

    /**
     * Verifies that a new stat can be registered and retrieved with the correct value.
     */
    @Test
    public void testRegisterStat() {
        stats.registerStat("magic", 50);
        assertEquals(50, stats.getStat("magic").get());
    }
}
