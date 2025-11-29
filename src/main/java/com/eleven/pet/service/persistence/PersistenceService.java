package com.eleven.pet.service.persistence;

import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.PetModel;

import java.nio.file.Path;

public class PersistenceService {
    private final EncryptionService encryptionService;
    private final Path savePath;
    // private final ObjectMapper objectMapper = new ObjectMapper();

    public PersistenceService(EncryptionService encryptionService, Path savePath) {
        this.encryptionService = encryptionService;
        this.savePath = savePath;
    }

    public void save(PetModel model) {
        // TODO: Implement saving data (with encryptionService)
        // 1. Create PetDataDTO from PetModel
        // 2. Serialize PetDataDTO to JSON (write to a ByteArrayOutputStream to support encryption)
        // 3. Encrypt JSON stream and write to file at savePath

        throw new UnsupportedOperationException("Not implemented yet");
    }

    public PetModel load(WeatherSystem weatherSystem, GameClock gameClock) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
