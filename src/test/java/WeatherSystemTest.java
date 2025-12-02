import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherSystemTest {

    @Test
    void testChangeWeather() {
        WeatherSystem weatherSystem = new WeatherSystem();
        weatherSystem.changeWeather();
        assertNotNull(weatherSystem.getCurrentWeather(), "Current weather should not be null after change");
    }

    @Test
    void testObserverNotification() {
        WeatherSystem weatherSystem = new WeatherSystem();
        
        // Create a simple listener to track if it was called
        final boolean[] listenerCalled = {false};
        final WeatherState[] receivedState = {null};
        
        WeatherListener testListener = new WeatherListener() {
            @Override
            public void onWeatherChange(WeatherState newState) {
                listenerCalled[0] = true;
                receivedState[0] = newState;
            }
        };
        
        weatherSystem.subscribe(testListener);
        weatherSystem.changeWeather();
        
        assertTrue(listenerCalled[0], "Listener should be called when weather changes");
        assertNotNull(receivedState[0], "Listener should receive a WeatherState");
    }
}
