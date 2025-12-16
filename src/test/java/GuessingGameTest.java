import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.GuessingGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the {@link GuessingGame} minigame.
 */
public class GuessingGameTest {

    /**
     * Verifies that at least one guess eventually wins and produces
     * a {@link MinigameResult} with a positive happiness delta.
     */
    @Test
    public void testGuessGameWin() {
        GuessingGame game = new GuessingGame();
        PetModel pet = new PetModel(null, null, null);

        // Generate the secret number once, then try all possible guesses
        game.generateNewNumber();
        MinigameResult result = null;
        for (int guess = game.getMinNumber(); guess <= game.getMaxNumber(); guess++) {
            result = game.checkGuess(guess);
            if (result.won()) {
                pet.getStats().modifyStat(PetStats.STAT_HAPPINESS, result.happinessDelta());
                break;
            }
        }

        assertNotNull(result);
        assertTrue(result.happinessDelta() > 0, "Winning should give positive happiness");
    }
}