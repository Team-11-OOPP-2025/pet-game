package com.eleven.pet.persistence;

import java.nio.file.Path;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;
import com.google.gson.Gson;

@SuppressWarnings("unused")
public class PersistenceService {
    private final EncryptionService crypto;
    private final Path savePath;
    private final Gson mapper;
    
    public PersistenceService(EncryptionService crypto, Path savePath) {
        this.crypto = crypto;
        this.savePath = savePath;
        this.mapper = new Gson();
    }
    
    public void save(PetModel model) throws Exception {
        // TODO: Implement save functionality
    }
    
    public PetModel load(WeatherSystem weatherSystem, GameClock clock) throws Exception {
        // TODO: Implement load functionality
        return null;
    }
}
