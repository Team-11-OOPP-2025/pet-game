import com.eleven.pet.environment.weather.CloudyState;
import com.eleven.pet.environment.weather.RainyState;
import com.eleven.pet.environment.weather.SunnyState;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.effects.ParticleEffect;
import com.eleven.pet.vfx.effects.RainParticleEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeatherParticleIntegrationTest {

    @Test
    void testRainyStateReturnsRainEffect() {
        WeatherState rainyState = new RainyState();
        
        ParticleEffect effect = rainyState.getParticleEffect();
        
        assertNotNull(effect, "Rainy state should return a particle effect");
        assertInstanceOf(RainParticleEffect.class, effect, 
            "Rainy state should return RainParticleEffect");
        
        RainParticleEffect rainEffect = (RainParticleEffect) effect;
        assertEquals(300, rainEffect.intensity(),
            "Rain effect should have intensity of 300");
    }

    @Test
    void testSunnyStateReturnsNoEffect() {
        WeatherState sunnyState = new SunnyState();
        
        ParticleEffect effect = sunnyState.getParticleEffect();
        
        assertNotNull(effect, "Sunny state should return a particle effect");
        assertInstanceOf(NoParticleEffect.class, effect, 
            "Sunny state should return NoParticleEffect (no particles)");
    }

    @Test
    void testCloudyStateReturnsNoEffect() {
        WeatherState cloudyState = new CloudyState();
        
        ParticleEffect effect = cloudyState.getParticleEffect();
        
        assertNotNull(effect, "Cloudy state should return a particle effect");
        assertInstanceOf(NoParticleEffect.class, effect, 
            "Cloudy state should return NoParticleEffect (no particles)");
    }

    @Test
    void testAllWeatherStatesHaveParticleEffects() {
        WeatherState[] states = {
            new RainyState(),
            new SunnyState(),
            new CloudyState()
        };
        
        for (WeatherState state : states) {
            ParticleEffect effect = state.getParticleEffect();
            assertNotNull(effect, 
                state.getName() + " should return a non-null particle effect");
        }
    }

    @Test
    void testRainEffectIntensity() {
        RainParticleEffect lightRain = new RainParticleEffect(25);
        RainParticleEffect heavyRain = new RainParticleEffect(100);

        assertEquals(25, lightRain.intensity(), "Light rain should have lower intensity");
        assertEquals(100, heavyRain.intensity(), "Heavy rain should have higher intensity");
    }
}
