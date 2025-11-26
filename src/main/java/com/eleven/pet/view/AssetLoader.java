package com.eleven.pet.view;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AssetLoader {
    private static AssetLoader instance;
    private final Map<String, Image> imageCache = new HashMap<>();


    private AssetLoader() {
       // Singleton isn't initialized
    }

    public static AssetLoader getInstance() {
        if (instance == null) {
            instance = new AssetLoader();
        }
        return instance;
    }

    public Image getImage(String assetName) {
        return imageCache.computeIfAbsent(assetName, this::loadImage);
    }

    private Image loadImage(String imageName) {
        String path = "/images/" + imageName + ".png";
        InputStream stream;
        try {
            stream = getClass().getResourceAsStream(path);

            if (stream == null) {
                path = "/images/" + imageName + ".jpg";
                stream = getClass().getResourceAsStream(path);
            }

            if (stream == null) {
                System.err.println("Warning: Image not found: " + path);
                return null;
            }

            return new Image(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void loadAll() {
        // Placeholder for loading all assets
    }
}
