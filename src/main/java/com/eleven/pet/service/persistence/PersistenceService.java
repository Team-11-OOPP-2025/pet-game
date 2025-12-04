package com.eleven.pet.service.persistence;

import com.eleven.pet.behavior.PetState;
import com.eleven.pet.behavior.StateRegistry;
import com.eleven.pet.config.GameConfig;
import com.eleven.pet.environment.clock.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.model.Inventory;
import com.eleven.pet.model.PetFactory;
import com.eleven.pet.model.PetModel;
import com.eleven.pet.model.PetStats;
import com.eleven.pet.model.items.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.IntegerProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PersistenceService {
    private final EncryptionService encryptionService;
    private final Path savePath;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public PersistenceService(EncryptionService encryptionService, Path savePath) {
        this.encryptionService = encryptionService;
        this.savePath = savePath;
    }

    public void save(PetModel model) throws GameException {
        try {
            PetDataDTO dto = new PetDataDTO(GameConfig.SAVE_FILE_VERSION);

            dto.setPetName(model.getName());
            dto.setCurrentStateName(model.getCurrentState().getStateName());
            dto.setLastSaveTimestamp(System.currentTimeMillis());

            dto.setStatsData(extractStats(model.getStats()));
            dto.setInventoryData(extractInventory(model.getInventory()));

            dto.setSleepStartTime(model.getSleepStartTime());
            dto.setSleptThisNight(model.getSleptThisNight());

            ByteArrayOutputStream jsonBuffer = new ByteArrayOutputStream();
            jsonMapper.writeValue(jsonBuffer, dto);
            try (InputStream sourceStream = new ByteArrayInputStream(jsonBuffer.toByteArray());
                 OutputStream fileOutput = Files.newOutputStream(savePath)) {
                encryptionService.encrypt(sourceStream, fileOutput);
            }

            System.out.println("Game data saved successfully to " + savePath);
        } catch (Exception e) {
            throw new GameException("Failed to save game data", e);
        }
    }

    public PetModel load(WeatherSystem weatherSystem, GameClock gameClock) throws GameException {
        if (!Files.exists(savePath)) {
            throw new GameException("Save file does not exist.");
        }

        try {
            PetDataDTO dto;
            try (InputStream fileInput = Files.newInputStream(savePath);
                 ByteArrayOutputStream decryptedBuffer = new ByteArrayOutputStream()) {

                encryptionService.decrypt(fileInput, decryptedBuffer);
                byte[] jsonBytes = decryptedBuffer.toByteArray();

                dto = jsonMapper.readValue(jsonBytes, PetDataDTO.class);
            }

            PetModel model = PetFactory.createNewPet(dto.getPetName(), weatherSystem, gameClock);

            String stateName = dto.getCurrentStateName();
            PetState restoredState = StateRegistry.getInstance().getState(stateName);

            if (restoredState != null) {
                model.changeState(restoredState);
            }

            applyStats(dto.getStatsData(), model.getStats());
            applyInventory(dto.getInventoryData(), model.getInventory());

            model.setSleepStartTime(dto.getSleepStartTime());
            model.setSleptThisNight(dto.isSleptThisNight());

            System.out.println("[" + dto.getVersion() + "] Game loaded successfully!");
            return model;

        } catch (Exception e) {
            throw new GameException("Failed to load game data. File may be corrupted.", e);
        }
    }

    private Map<String, Integer> extractStats(PetStats stats) {
        Map<String, Integer> data = new HashMap<>();
        for (Map.Entry<String, IntegerProperty> entry : stats.getAllStats().entrySet()) {
            data.put(entry.getKey(), entry.getValue().get());
        }
        return data;
    }

    private void applyStats(Map<String, Integer> data, PetStats stats) {
        if (data == null) return;
        // if stats already registered, just set; otherwise you may register then set
        data.forEach(stats::registerStat);
    }

    private Map<Integer, Integer> extractInventory(Inventory inventory) {
        return inventory.getAllOwnedItems();
    }

    private void applyInventory(Map<Integer, Integer> data, Inventory inventory) {
        if (data == null) return;
        data.forEach((id, qty) -> {
            Item item = com.eleven.pet.data.ItemRegistry.get(id);
            if (item != null) {
                inventory.add(item, qty);
            }
        });
    }
}
