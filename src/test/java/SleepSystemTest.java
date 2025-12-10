import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.GameClock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        pet = PetFactory.createNewPet("TestPet", null, clock, null);
    }

    @Test
    void testSleepButtonRestoresStats() {
        // Set initial low stats
        pet.getStats().modifyStat(PetStats.STAT_ENERGY, -40); // 50 - 40 = 10
        pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, -30); // 50 - 30 = 20

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessBefore = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Press sleep button (starts sleep timer, no instant reward)
        pet.requestSleepInteraction();

        // Verify no immediate change
        int energyAfterClick = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        assertEquals(energyBefore, energyAfterClick, "Stats should not increase instantly");

        // Simulate 1 hour passing
        double secondsPerGameHour = GameConfig.DAY_LENGTH_SECONDS / 24.0;
        clock.tick(secondsPerGameHour);

        int energyAfterHour = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessAfterHour = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Verify hourly stats increased
        assertEquals(GameConfig.SLEEP_ENERGY_PER_HOUR, energyAfterHour - energyBefore, "Energy should increase by hourly rate");
        assertEquals(GameConfig.SLEEP_HAPPINESS_PER_HOUR, happinessAfterHour - happinessBefore, "Happiness should increase by hourly rate");
        assertTrue(pet.isSleptThisNight(), "Pet should be marked as having slept");
    }

    @Test
    void testMissedSleepAppliesPenalty() {
        // Start at 12:00, advance to 20:00 to reset sleep flag
        double targetTime20 = (20.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        double currentTime = clock.getGameTime();
        double timeDelta1 = targetTime20 - currentTime;

        clock.tick(timeDelta1);
        pet.onTick(timeDelta1);

        // Advance to just before 8 AM without sleeping (through midnight)
        // We add ~12 hours to cross midnight safely
        double hoursToAdvance = 11.9;
        double secondsToAdvance = hoursToAdvance * (GameConfig.DAY_LENGTH_SECONDS / 24.0);

        clock.tick(secondsToAdvance);
        pet.onTick(secondsToAdvance);

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessBefore = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Cross 8 AM threshold
        double tickAcross = 0.2 * (GameConfig.DAY_LENGTH_SECONDS / 24.0);
        clock.tick(tickAcross);
        pet.onTick(tickAcross);

        int energyAfter = pet.getStats().getStat(PetStats.STAT_ENERGY).get();
        int happinessAfter = pet.getStats().getStat(PetStats.STAT_HAPPINESS).get();

        // Verify penalty was applied
        assertEquals(-GameConfig.MISSED_SLEEP_ENERGY_PENALTY, energyAfter - energyBefore, "Energy should decrease by penalty");
        assertTrue(happinessAfter < happinessBefore);
    }

    @Test
    void testSleepingPreventsNoPenalty() {
        // Advance to sleep window (20:00)
        double targetTime = (20.0 / 24.0) * GameConfig.DAY_LENGTH_SECONDS;
        double timeDelta = targetTime - clock.getGameTime();
        clock.tick(timeDelta);
        pet.onTick(timeDelta);

        // Sleep during the night
        pet.requestSleepInteraction();

        int energyBefore = pet.getStats().getStat(PetStats.STAT_ENERGY).get();

        // Advance to 8 AM (approx 12 hours later)
        double sleepDuration = 12.0 * (GameConfig.DAY_LENGTH_SECONDS / 24.0);
        clock.tick(sleepDuration);
        pet.onTick(sleepDuration);

        // Cross 8 AM to trigger auto-wake
        double nudging = 0.1;
        clock.tick(nudging);
        pet.onTick(nudging);

        int energyAfter = pet.getStats().getStat(PetStats.STAT_ENERGY).get();

        // Verify pet woke up and gained energy (instead of losing it)
        assertInstanceOf(AwakeState.class, pet.getCurrentState(), "Pet should auto-wake");
        assertTrue(energyAfter > energyBefore, "Energy should increase from sleeping");
    }
}