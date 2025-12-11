import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PetStatsTest {

    PetStats stats;

    @BeforeEach
    public void setup() {
        PetModel petModel = new PetModel(null, null, null);
        stats = petModel.getStats();
    }

    @Test
    public void testStatClampingMax() {
        stats.modifyStat(PetStats.STAT_HAPPINESS, 150);
        assertEquals(100, stats.getStat(PetStats.STAT_HAPPINESS).get());
    }

    @Test
    public void testStatClampingMin() {
        stats.modifyStat(PetStats.STAT_HAPPINESS, -200);
        assertEquals(0, stats.getStat(PetStats.STAT_HAPPINESS).get());
    }

    @Test
    public void testRegisterStat() {
        stats.registerStat("magic", 50);
        assertEquals(50, stats.getStat("magic").get());
    }
}
