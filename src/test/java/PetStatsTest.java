import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assert (stats.getStat(PetStats.STAT_HAPPINESS).get() == 100);
    }

    @Test
    public void testStatClampingMin() {
        stats.modifyStat(PetStats.STAT_HAPPINESS, -200);
        assert (stats.getStat(PetStats.STAT_HAPPINESS).get() == 0);
    }

    @Test
    public void testRegisterStat() {
        stats.registerStat("magic", 50);
        assert (stats.getStat("magic").get() == 50);
    }
}
