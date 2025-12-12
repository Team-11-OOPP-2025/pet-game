import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.eleven.pet.vfx.Particle;
import com.eleven.pet.vfx.ParticleSystem;
import com.eleven.pet.vfx.RainParticle;
import com.eleven.pet.vfx.RainParticleFactory;

public class ParticleSystemTest {

    @Test
    void testParticleUpdate() {
        // Create a rain particle
        RainParticle particle = new RainParticle(100, 100, 200);
        
        // Store initial position
        double initialY = particle.getY();
        double initialVelocityY = particle.getVelocityY();
        
        // Update particle (simulate 0.1 seconds)
        particle.update(0.1);
        
        // Assert position changed (particle moved down)
        assertTrue(particle.getY() > initialY, "Particle Y position should increase after update");
        
        // Assert velocity increased due to gravity
        assertTrue(particle.getVelocityY() > initialVelocityY, "Particle velocity should increase due to gravity");
        
        // Particle should still be alive
        assertTrue(particle.isAlive(), "Particle should be alive");
    }

    @Test
    void testRainFactory() {
        RainParticleFactory factory = new RainParticleFactory();
        
        // Create a particle
        Particle particle = factory.createParticle(800, 600);
        
        // Assert particle was created
        assertNotNull(particle, "Factory should create a non-null particle");
        
        // Assert it's a RainParticle
        assertInstanceOf(RainParticle.class, particle, "Factory should create RainParticle instances");
        
        // Assert particle has high Y velocity (rain falls fast)
        assertTrue(particle.getVelocityY() >= 200.0, 
            "Rain particle should have high Y velocity (at least 200)");
        
        // Assert particle starts above visible area
        assertTrue(particle.getY() < 0, 
            "Rain particle should start above the visible area");
        
        // Assert particle is within horizontal bounds
        assertTrue(particle.getX() >= 0 && particle.getX() <= 800,
            "Particle X should be within canvas width");
    }

    @Test
    void testParticleSystemCreation() {
        ParticleSystem system = new ParticleSystem(800, 600);
        
        assertNotNull(system.getCanvas(), "ParticleSystem should have a canvas");
        assertEquals(800, system.getCanvas().getWidth(), "Canvas width should match");
        assertEquals(600, system.getCanvas().getHeight(), "Canvas height should match");
    }

    @Test
    void testParticleSystemWithFactory() {
        ParticleSystem system = new ParticleSystem(800, 600);
        RainParticleFactory factory = new RainParticleFactory();
        
        system.setParticleFactory(factory);
        
        // Note: Cannot test startAnimation in unit test without JavaFX toolkit
        // This would be tested in integration tests
        
        // Test that getParticleCount starts at 0
        assertEquals(0, system.getParticleCount(), "Particle count should start at 0");
    }

    @Test
    void testMultipleParticleUpdates() {
        RainParticle particle = new RainParticle(100, 100, 200);
        
        double initialY = particle.getY();
        
        // Update multiple times
        for (int i = 0; i < 10; i++) {
            particle.update(0.016); // ~60 FPS
        }
        
        // Position should have changed significantly
        assertTrue(particle.getY() > initialY + 10, 
            "Particle should move significantly after multiple updates");
    }
}
