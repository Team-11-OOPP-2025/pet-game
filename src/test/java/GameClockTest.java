import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.DayCycle;
import com.eleven.pet.environment.clock.GameClock;

public class GameClockTest {
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
        double targetNormalizedTime = normalizedTime;
        double delta = targetNormalizedTime - currentNormalizedTime;
        if (delta < 0) {
            delta += 1.0; // Wrap around through midnight
        }
        clock.tick(delta * GameConfig.DAY_LENGTH_SECONDS);
        assertEquals(expected, clock.getCycle());
    }

    @Test
    void testTickIncrementsTime() {
        GameClock clock = new GameClock();
        double initialTime = clock.getGameTime();
        clock.tick(1.0);
        double finalTime = clock.getGameTime();
        assertTrue(finalTime > initialTime, "Game time should increase after tick");
    }

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
