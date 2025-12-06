import com.eleven.pet.character.PetModel;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.GuessingGame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuessingGameTest {

@Test
public void testGuessGameWin() {
    GuessingGame game = new GuessingGame();
    PetModel pet = new PetModel(null, null, null);
    
    // Generate the secret number once, then try all possible guesses
    game.generateNewNumber();
    MinigameResult result = null;
    for (int guess = game.getMinNumber(); guess <= game.getMaxNumber(); guess++) {
        result = game.checkGuess(guess, pet);
        if (result.isWon()) {
            break;
        }
    }
    
    assertNotNull(result);
    assertTrue(result.getHappinessDelta() > 0, "Winning should give positive happiness");
}

}