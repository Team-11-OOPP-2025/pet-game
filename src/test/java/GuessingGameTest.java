import org.junit.jupiter.api.Test;
import com.eleven.pet.model.GuessingGame;
import com.eleven.pet.model.MinigameResult;
import com.eleven.pet.model.PetModel;

import static org.junit.jupiter.api.Assertions.*;

public class GuessingGameTest {

@Test
public void testGuessGameWin() {
    GuessingGame game = new GuessingGame();
    PetModel pet = new PetModel(null, null, null);
    
    // Try all possible numbers to ensure we get a win
    MinigameResult result = null;
    for (int guess = game.getMinNumber(); guess <= game.getMaxNumber(); guess++) {
        game.generateNewNumber();
        result = game.checkGuess(guess, pet);
        if (result.isWon()) {
            break;
        }
    }
    
    assertNotNull(result);
    assertTrue(result.getHappinessDelta() > 0, "Winning should give positive happiness");
}

}