import com.eleven.pet.behavior.AsleepState;
import com.eleven.pet.behavior.AwakeState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SleepSystemTest {

    private PetModel pet;
    private GameClock clock;

    @BeforeEach
    void setUp() {
        // Manually register states for testing since AutoService doesn't work in tests
        StateRegistry registry = StateRegistry.getInstance();
        registry.registerState(new AwakeState());
        registry.registerState(new AsleepState());

        clock = new GameClock();
        pet = PetFactory.createNewPet("TestPet", null, clock);
    }

    @Test
    void testSleepButtonRestoresStats() {
        // Set initial low stats

        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -40); // 50 - 40 = 10

        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, -30); // 50 - 30 = 20

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessBefore = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Press sleep button
        pet.performSleep();

        int energyAfter = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessAfter = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Verify stats increased
        assertEquals(40, energyAfter - energyBefore, "Energy should increase by 40");
        assertEquals(20, happinessAfter - happinessBefore, "Happiness should increase by 20");
        assertTrue(pet.hasSleptThisNight(), "Pet should be marked as having slept");
    }

    @Test
    void testMissedSleepAppliesPenalty() {
        // Start at 12:00, advance to 20:00 to reset sleep flag
        double targetTime20 = (20.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        double currentTime = clock.getGameTime();
        double timeDelta1 = targetTime20 - currentTime;

        clock.tick(timeDelta1);
        pet.onTick(timeDelta1); // This should reset sleep flag and set passedEightAM to false

        // Advance to just before 8 AM without sleeping (through midnight)
        double targetTime8 = (7.9 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        double timeDelta2 = (GameConfig.DAY_LENGTH_SECONDS - targetTime20) + targetTime8;

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessBefore = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Tick to just before 8 AM
        clock.tick(timeDelta2);
        pet.onTick(timeDelta2);

        // Now cross 8 AM - this should trigger the penalty
        clock.tick(0.2);
        pet.onTick(0.2);

        int energyAfter = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessAfter = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Verify penalty was applied
        assertEquals(-30, energyAfter - energyBefore, "Energy should decrease by 30");
        assertEquals(-20, happinessAfter - happinessBefore, "Happiness should decrease by 20");
    }

    @Test
    void testSleepingPreventsNoPenalty() {
        // Advance to sleep window (20:00)
        double targetTime = (20.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        double timeDelta = targetTime - clock.getGameTime();
        clock.tick(timeDelta);
        pet.onTick(timeDelta);

        // Sleep during the night
        pet.performSleep();

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessBefore = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Advance to 8 AM
        targetTime = (8.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        timeDelta = (GameConfig.DAY_LENGTH_SECONDS - clock.getGameTime()) + targetTime;
        clock.tick(timeDelta);
        pet.onTick(timeDelta);

        // Cross 8 AM
        clock.tick(0.1);
        pet.onTick(0.1);

        int energyAfter = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessAfter = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Verify NO penalty was applied (stats should be the same)
        assertEquals(0, energyAfter - energyBefore, "Energy should not decrease when pet slept");
        assertEquals(0, happinessAfter - happinessBefore, "Happiness should not decrease when pet slept");
    }
}
