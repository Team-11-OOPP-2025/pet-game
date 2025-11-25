package com.eleven.pet.persistence;

import java.nio.file.Path;

import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;

public class PersistenceService {
    private final EncryptionService crypto;
    private final Path savePath;
    private final Object mapper;
    
    public PersistenceService(EncryptionService crypto, Path savePath) {
        this.crypto = crypto;
        this.savePath = savePath;
        this.mapper = null;
    }
    
    public void save(PetModel model) throws Exception {
    }
    
    public PetModel load(WeatherSystem weatherSystem, GameClock clock) throws Exception {
        return null;
    }
}
