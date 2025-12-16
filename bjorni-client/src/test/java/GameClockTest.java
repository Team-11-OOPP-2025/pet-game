import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.DayCycle;
import com.eleven.pet.environment.time.GameClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link GameClock} time progression and day-cycle calculation.
 */
public class GameClockTest {

    /**
     * Verifies that the {@link GameClock} calculates the correct {@link DayCycle}
     * for various normalized times of day.
     *
     * @param normalizedTime normalized time of day in [0,1)
     * @param expected       expected {@link DayCycle} value
     */
    @ParameterizedTest
    @CsvSource({
            "0.0, DEEP_NIGHT",
            "0.17, DAWN",
            "0.30, MORNING",
            "0.50, DAY",
            "0.71, EVENING",
            "0.88, EARLY_NIGHT"
    })
    void testCycleCalculation(double normalizedTime, DayCycle expected) {
        GameClock clock = new GameClock();
        // Clock starts at 12:00 (0.5 of the day), so we need to calculate delta from there
        double currentNormalizedTime = 0.5; // 12:00
        double delta = normalizedTime - currentNormalizedTime;
        if (delta < 0) {
            delta += 1.0; // Wrap around through midnight
        }
        clock.tick(delta * GameConfig.DAY_LENGTH_SECONDS);
        assertEquals(expected, clock.getCycle());
    }

    /**
     * Ensures that ticking the clock advances the game time.
     */
    @Test
    void testTickIncrementsTime() {
        GameClock clock = new GameClock();
        double initialTime = clock.getGameTime();
        clock.tick(1.0);
        double finalTime = clock.getGameTime();
        assertTrue(finalTime > initialTime, "Game time should increase after tick");
    }

    /**
     * Ensures that when the clock is paused, ticking does not advance game time.
     */
    @Test
    void testPauseLogic() {
        GameClock clock = new GameClock();
        double initialTime = clock.getGameTime();
        clock.setPaused(true);
        clock.tick(1.0);
        double finalTime = clock.getGameTime();
        assertEquals(initialTime, finalTime, "Game time should not change when paused");
    }
}
