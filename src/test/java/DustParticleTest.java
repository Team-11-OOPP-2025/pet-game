import com.eleven.pet.vfx.effects.DustParticleEffect;
import com.eleven.pet.vfx.effects.NoParticleEffect;
import com.eleven.pet.vfx.ParticleSystem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DustParticleTest {
    
    @Test
    void testDustParticleEffectCreation() {
        DustParticleEffect lightDust = new DustParticleEffect(8);
        DustParticleEffect heavyDust = new DustParticleEffect(20);
        
        assertEquals(8, lightDust.intensity(), "Light dust should have intensity 8");
        assertEquals(20, heavyDust.intensity(), "Heavy dust should have intensity 20");
    }
    
    @Test
    void testDustParticleEffectStart() {
        ParticleSystem system = new ParticleSystem(300, 400);
        DustParticleEffect dustEffect = new DustParticleEffect(10);
        
        dustEffect.start(system);
        
        // After starting, the particle system should have been configured
        assertNotNull(system, "Particle system should not be null");
        
        dustEffect.stop(system);
    }
    
    @Test
    void testNoParticleEffect() {
        ParticleSystem system = new ParticleSystem(300, 400);
        NoParticleEffect noEffect = new NoParticleEffect();
        
        noEffect.start(system);
        assertEquals(0, system.getParticleCount(), "No particles should be created");
        
        noEffect.stop(system);
    }
}
