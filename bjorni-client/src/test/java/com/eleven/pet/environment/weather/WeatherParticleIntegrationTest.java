package com.eleven.pet.environment.weather;

import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.effects.ParticleEffect;
import com.eleven.pet.vfx.effects.RainParticleEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the mapping between {@link WeatherState} implementations
 * and their corresponding {@link ParticleEffect} visualizations.
 */
public class WeatherParticleIntegrationTest {

    /**
     * Ensures {@link RainyState} produces a {@link RainParticleEffect}
     * with the expected default intensity.
     */
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

    /**
     * Ensures {@link SunnyState} produces a {@link NoParticleEffect},
     * meaning no visible particles are rendered.
     */
    @Test
    void testSunnyStateReturnsNoEffect() {
        WeatherState sunnyState = new SunnyState();
        
        ParticleEffect effect = sunnyState.getParticleEffect();
        
        assertNotNull(effect, "Sunny state should return a particle effect");
        assertInstanceOf(NoParticleEffect.class, effect, 
            "Sunny state should return NoParticleEffect (no particles)");
    }

    /**
     * Ensures {@link CloudyState} produces a {@link NoParticleEffect},
     * meaning cloudy weather does not emit particles.
     */
    @Test
    void testCloudyStateReturnsNoEffect() {
        WeatherState cloudyState = new CloudyState();
        
        ParticleEffect effect = cloudyState.getParticleEffect();
        
        assertNotNull(effect, "Cloudy state should return a particle effect");
        assertInstanceOf(NoParticleEffect.class, effect, 
            "Cloudy state should return NoParticleEffect (no particles)");
    }

    /**
     * Sanity check that all known {@link WeatherState} implementations
     * return a non-{@code null} {@link ParticleEffect}.
     */
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

    /**
     * Verifies that {@link RainParticleEffect} correctly exposes the
     * configured rain intensity used by the renderer.
     */
    @Test
    void testRainEffectIntensity() {
        RainParticleEffect lightRain = new RainParticleEffect(25);
        RainParticleEffect heavyRain = new RainParticleEffect(100);

        assertEquals(25, lightRain.intensity(), "Light rain should have lower intensity");
        assertEquals(100, heavyRain.intensity(), "Heavy rain should have higher intensity");
    }
}
