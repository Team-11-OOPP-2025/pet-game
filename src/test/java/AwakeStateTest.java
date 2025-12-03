import com.eleven.pet.behavior.AwakeState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class AwakeStateTest {

    @Test
    void testStateNames() {
        AwakeState awakeState = new AwakeState();
        assertEquals("awake", awakeState.getStateName());
    }

}
