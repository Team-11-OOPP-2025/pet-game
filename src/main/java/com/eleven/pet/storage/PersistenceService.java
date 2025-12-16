package com.eleven.pet.storage;

import com.eleven.pet.character.PetFactory;
import com.eleven.pet.character.PetModel;
import com.eleven.pet.character.PetStats;
import com.eleven.pet.character.behavior.AsleepState;
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

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handles persistence of {@link com.eleven.pet.character.PetModel} instances to a
 * single encrypted save file and loading them back into memory.
 * <p>
 * This service is responsible for:
 * <ul>
 *     <li>Serializing the pet model into a {@link com.eleven.pet.storage.dto.PetDataDTO}</li>
 *     <li>Encrypting and writing save data to disk</li>
 *     <li>Decrypting and reading save data from disk</li>
 *     <li>Mapping DTO data back into a live {@link com.eleven.pet.character.PetModel}</li>
 * </ul>
 * The actual crypto operations are delegated to {@link EncryptionService}.
 */
public class PersistenceService {
    private final EncryptionService encryptionService;
    private final Path savePath;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    /**
     * Creates a new persistence service that reads from and writes to the given path
     * using the provided {@link EncryptionService}.
     *
     * @param encryptionService encryption wrapper used to encrypt/decrypt save data
     * @param savePath          filesystem path of the save file
     */
    public PersistenceService(EncryptionService encryptionService, Path savePath) {
        this.encryptionService = encryptionService;
        this.savePath = savePath;
    }

    /**
     * Persist the current {@link PetModel} state to the configured save file.
     * <p>
     * Data is first mapped into {@link PetDataDTO}, then written as JSON through
     * an encrypted output stream.
     *
     * @param model the pet model to save
     * @throws GameException if any I/O, serialization, or encryption error occurs
     */
    public void save(PetModel model) throws GameException {
        try {
            PetDataDTO dto = new PetDataDTO(GameConfig.SAVE_FILE_VERSION);

            dto.setPetName(model.getName());
            dto.setTutorialCompleted(model.isTutorialCompleted());
            dto.setCurrentStateName(model.getCurrentState().getStateName());
            dto.setLastSaveTimestamp(System.currentTimeMillis());

            dto.setStatsData(extractStats(model.getStats()));
            dto.setInventoryData(extractInventory(model.getInventory()));

            dto.setSleepStartTime(model.getCurrentSleepDuration());
            dto.setSleptThisNight(model.isSleptThisNight());

            // Save reward cooldown
            dto.setRewardCooldown(model.getRewardCooldown());

            try (OutputStream fileOut = Files.newOutputStream(savePath);
                 OutputStream encryptedOut = encryptionService.wrapOutputStream(fileOut)) {
                jsonMapper.writeValue(encryptedOut, dto);

            }

            System.out.println("Game data saved successfully to " + savePath);
        } catch (Exception e) {
            throw new GameException("Failed to save game data", e);
        }
    }

    /**
     * Load a previously saved {@link PetModel} from the configured save file.
     * <p>
     * If the file does not exist, an empty {@link Optional} is returned. If it does
     * exist, the file is decrypted, deserialized into {@link PetDataDTO}, then
     * mapped into a fresh {@link PetModel} instance created by {@link PetFactory}.
     *
     * @param weatherSystem the weather system to associate with the loaded pet
     * @param gameClock     the game clock to associate with the loaded pet
     * @return an {@link Optional} containing the restored model, or empty if no save exists
     * @throws GameException if the file exists but cannot be read, decrypted, or parsed
     */
    public Optional<PetModel> load(WeatherSystem weatherSystem, GameClock gameClock) throws GameException {
        if (!Files.exists(savePath)) {
            return Optional.empty();
        }

        try {
            PetDataDTO dto;
            try (InputStream fileInput = Files.newInputStream(savePath);
                 // Decrypt the input stream on-the-fly
                 InputStream decryptedInput = encryptionService.wrapInputStream(fileInput)) {

                // Read all bytes from the decrypted stream
                dto = jsonMapper.readValue(decryptedInput, PetDataDTO.class);
            }

            PetModel model = PetFactory.createNewPet(dto.getPetName(), weatherSystem, gameClock);

            String stateName = dto.getCurrentStateName();
            PetState restoredState = StateRegistry.getInstance().getState(stateName);

            // Prevent loading into AsleepState directly as the clock may have advanced
            if (restoredState != null && !(restoredState instanceof AsleepState)) {
                model.changeState(restoredState);
            }

            applyStats(dto.getStatsData(), model.getStats());
            applyInventory(dto.getInventoryData(), model.getInventory());

            model.setTutorialCompleted(dto.isTutorialCompleted());
            model.setCurrentSleepDuration(dto.getSleepStartTime());
            model.setSleptThisNight(dto.isSleptThisNight());

            // Load reward cooldown
            model.setRewardCooldown(dto.getRewardCooldown());

            System.out.println("[" + dto.getVersion() + "] Game loaded successfully!");
            return Optional.of(model);

        } catch (Exception e) {
            throw new GameException("Failed to load game data. File may be corrupted.", e);
        }
    }

    /**
     * Extract a flat map of stat name to integer value from the given {@link PetStats}.
     *
     * @param stats the stats container, may be {@code null}
     * @return a non-null map of stat name to value; empty if no stats are available
     */
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

    /**
     * Apply persisted stats to the given {@link PetStats} instance.
     * <p>
     * For each entry, the stat is registered (or overwritten) with the stored value.
     *
     * @param data  map of stat name to value, may be {@code null}
     * @param stats target stats container, may be {@code null}
     */
    private void applyStats(Map<String, Integer> data, PetStats stats) {
        if (data == null || stats == null) return;
        // if stats already registered, just set; otherwise you may register then set
        data.forEach(stats::registerStat);
    }

    /**
     * Extract a copy of the inventory as a map of item id to quantity.
     *
     * @param inventory the source inventory, may be {@code null}
     * @return a new mutable map containing all owned items, never {@code null}
     */
    private Map<Integer, Integer> extractInventory(Inventory inventory) {
        if (inventory == null) return new HashMap<>();
        // Return a copy to avoid external modification
        return new HashMap<>(inventory.getAllOwnedItems());
    }

    /**
     * Replace the contents of the given {@link Inventory} with the provided data.
     * <p>
     * Existing items are cleared and then re-added based on the saved map.
     * Unknown item ids are ignored.
     *
     * @param data      map of item id to quantity, may be {@code null}
     * @param inventory target inventory, may be {@code null}
     */
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