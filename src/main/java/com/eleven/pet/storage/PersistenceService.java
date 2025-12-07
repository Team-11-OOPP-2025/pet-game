package com.eleven.pet.storage;

import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.core.GameException;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.storage.dto.PetDataDTO;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

            dto.setSleepStartTime(model.getCurrentSleepDuration());
            dto.setSleptThisNight(model.isSleptThisNight());

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

    /**
     * Load pet data from the save file.
     *
     * @param weatherSystem the weather system to attach to the loaded pet
     * @param gameClock     the game clock to attach to the loaded pet
     * @return Optional containing the loaded pet model, or empty if no save file exists
     * @throws GameException if the save file exists but cannot be read or is corrupted
     */
    public Optional<PetModel> load(WeatherSystem weatherSystem, GameClock gameClock) throws GameException {
        if (!Files.exists(savePath)) {
            return Optional.empty();
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

            model.setCurrentSleepDuration(dto.getSleepStartTime());
            model.setSleptThisNight(dto.isSleptThisNight());

            System.out.println("[" + dto.getVersion() + "] Game loaded successfully!");
            return Optional.of(model);

        } catch (Exception e) {
            throw new GameException("Failed to load game data. File may be corrupted.", e);
        }
    }

    private Map<String, Integer> extractStats(PetStats stats) {
        return Optional.ofNullable(stats)
                .map(PetStats::getAllStats)
                .orElse(Collections.emptyMap()) // Returns empty map if stats or allStats is null
                .entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get()
                ));
    }

    private void applyStats(Map<String, Integer> data, PetStats stats) {
        if (data == null || stats == null) return;
        // if stats already registered, just set; otherwise you may register then set
        data.forEach(stats::registerStat);
    }

    private Map<Integer, Integer> extractInventory(Inventory inventory) {
        if (inventory == null) return new HashMap<>();
        // Return a copy to avoid external modification
        return new HashMap<>(inventory.getAllOwnedItems());
    }

    private void applyInventory(Map<Integer, Integer> data, Inventory inventory) {
        if (data == null || inventory == null) return;

        // 1) Clear current inventory (including any default/replenished items)
        // (by default there will be no items in inventory at this point)
        inventory.getAllOwnedItems().forEach((id, qty) ->
                Optional.ofNullable(ItemRegistry.get(id))
                        .ifPresent(item -> inventory.remove(item, qty))
        );

        // 2) Apply saved inventory exactly
        data.forEach((id, qty) -> Optional.ofNullable(ItemRegistry.get(id))
                .ifPresent(item -> inventory.add(item, qty)));
    }

}
