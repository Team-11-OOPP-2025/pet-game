import com.eleven.pet.vfx.effects.DustParticleEffect;
import com.eleven.pet.vfx.effects.NoParticleEffect;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DustParticleTest {
    
    @Test
    void testDustParticleEffectCreation() {
        DustParticleEffect lightDust = new DustParticleEffect(8);
        DustParticleEffect heavyDust = new DustParticleEffect(20);
        
        assertEquals(8, lightDust.intensity(), "Light dust should have intensity 8");
        assertEquals(20, heavyDust.intensity(), "Heavy dust should have intensity 20");
    }
    
    @Test
    void testDustParticleEffectNotNull() {
        DustParticleEffect dustEffect = new DustParticleEffect(10);
        
        assertNotNull(dustEffect, "Dust effect should not be null");
        assertEquals(10, dustEffect.intensity(), "Dust effect should have correct intensity");
    }
    
    @Test
    void testNoParticleEffectCreation() {
        NoParticleEffect noEffect = new NoParticleEffect();
        
        assertNotNull(noEffect, "No particle effect should not be null");
    }
}
