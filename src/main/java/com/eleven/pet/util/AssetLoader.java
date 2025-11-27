package com.eleven.pet.util;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.image.Image;

@SuppressWarnings("unused")
public class AssetLoader {
    private static AssetLoader instance;
    private final Map<String, Image> imageCache;
    
    private AssetLoader() {
        this.imageCache = new HashMap<>();
    }
    
    public static AssetLoader getInstance() {
        if (instance == null) {
            instance = new AssetLoader();
        }
        return instance;
    }
    
    public Image getImage(String name) {
        return null;
    }
    
    private Image loadImage(String name) {
        return null;
    }
    
    public void loadAll() {
    }
}
