package com.eleven.pet.persistence;

import com.eleven.pet.model.PetModel;
import com.google.gson.*;
import javafx.beans.property.*;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Handles saving and loading the game state to a JSON file.
 */
public class PersistenceService {

    private final Path savePath;
    private final Gson gson;

    /**
     * Initializes the PersistenceService with the specified save file path.
     * @param savePath Path to the JSON save file.
     */
    public PersistenceService(Path savePath) {
        this.savePath = savePath;

        // Configure Gson to understand JavaFX Properties
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();

        // Adapters for JavaFX Properties serialization/deserialization
        // SimpleIntegerProperty adapter
        builder.registerTypeAdapter(SimpleIntegerProperty.class, (JsonSerializer<SimpleIntegerProperty>) (src, _, _) ->
                // Save as integer value
                new JsonPrimitive(src.get())
        );
        builder.registerTypeAdapter(SimpleIntegerProperty.class, (JsonDeserializer<SimpleIntegerProperty>) (json, _, _) ->
                // Load from integer value
                new SimpleIntegerProperty(json.getAsInt())
        );

        // SimpleDoubleProperty adapter
        builder.registerTypeAdapter(SimpleDoubleProperty.class, (JsonSerializer<SimpleDoubleProperty>) (src, _, _) ->
                new JsonPrimitive(src.get())
        );
        builder.registerTypeAdapter(SimpleDoubleProperty.class, (JsonDeserializer<SimpleDoubleProperty>) (json, _, _) ->
                new SimpleDoubleProperty(json.getAsDouble())
        );

        // SimpleBooleanProperty adapter
        builder.registerTypeAdapter(SimpleBooleanProperty.class, (JsonSerializer<SimpleBooleanProperty>) (src, _, _) ->
                new JsonPrimitive(src.get())
        );
        builder.registerTypeAdapter(SimpleBooleanProperty.class, (JsonDeserializer<SimpleBooleanProperty>) (json, _, _) ->
                new SimpleBooleanProperty(json.getAsBoolean())
        );

        // SimpleStringProperty adapter
        builder.registerTypeAdapter(SimpleStringProperty.class, (JsonSerializer<SimpleStringProperty>) (src, _, _) ->
                new JsonPrimitive(src.get())
        );
        builder.registerTypeAdapter(SimpleStringProperty.class, (JsonDeserializer<SimpleStringProperty>) (json, _, _) ->
                new SimpleStringProperty(json.getAsString())
        );

        // TODO: Add adapters for other Property types as needed

        this.gson = builder.create();
    }

    /**
     * Saves the current game state to a JSON file.
     * @param model The model to save.
     */
    public void save(PetModel model) {
        try (Writer writer = Files.newBufferedWriter(savePath)) {
            gson.toJson(model, writer);
        } catch (IOException e) {
            System.err.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads the game state from a JSON file.
     * @return The loaded PetModel, or null if no save exists.
     */
    public PetModel load() {
        if (!Files.exists(savePath)) {
            return null;
        }
        try (Reader reader = Files.newBufferedReader(savePath)) {
            return gson.fromJson(reader, PetModel.class);
        } catch (IOException e) {
            System.err.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}