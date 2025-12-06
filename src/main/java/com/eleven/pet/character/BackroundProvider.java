package com.eleven.pet.character;

import java.util.EnumMap;
import java.util.Map;

import com.eleven.pet.environment.time.DayCycle;

import javafx.scene.image.Image;

public class BackroundProvider {

    private final Map<DayCycle, Image> backgrounds = new EnumMap<>(DayCycle.class);
    private final Image defaultBackground;

    public BackroundProvider(Image defaultBackground) {
        this.defaultBackground = defaultBackground;
    }

    public void register(DayCycle cycle, Image background) {
        
        backgrounds.put(cycle, background);
    }

    public void normalize() {
        // 1. Handle special fallback: EARLY_NIGHT defaults to DEEP_NIGHT
        if (!backgrounds.containsKey(DayCycle.EARLY_NIGHT)) {
            Image deepNight = backgrounds.get(DayCycle.DEEP_NIGHT);
            if (deepNight != null) {
                backgrounds.put(DayCycle.EARLY_NIGHT, deepNight);
            }
        }

        // 2. Handle generic fallback: Everything else defaults to the 'defaultImage' (Day)
        for (DayCycle cycle : DayCycle.values()) {
            backgrounds.putIfAbsent(cycle, defaultBackground);
        }
    }

    public Image getimage(DayCycle cycle) {
        return backgrounds.get(cycle);
    }


}
   
    
    
    

