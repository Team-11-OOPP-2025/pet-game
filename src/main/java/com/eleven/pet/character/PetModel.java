package com.eleven.pet.character;

import com.eleven.pet.character.behavior.AsleepState;
import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.PetDefinition;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.time.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.inventory.StatPotionDefinition;
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.TimingGame;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core model representing the virtual pet, its stats, state, inventory and interactions with
 * environment systems such as clock and weather.
 */
@Data
public class PetModel implements TimeListener, WeatherListener {
    private static final java.util.Random random = new java.util.Random();

    private final String name;
    private final PetStats stats = new PetStats();
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private final Inventory inventory = new Inventory();

    private double hungerDecayAccum = 0.0;
    private double cleanlinessDecayAccum = 0.0;
    private double happinessDecayAccum = 0.0;

    private boolean sleptThisNight = false;
    private boolean passedEightAM = false;
    private double currentSleepDuration = 0.0;
    private int hoursSleptRewardCount = 0;

    private PetDefinition definition;

    // Thread-safe list to handle concurrent modifications during ticks
    private final List<ActivePotion> activePotions = new CopyOnWriteArrayList<>();

    // --- Inner Class to Track State ---
    private static class ActivePotion {
        private final StatPotionDefinition def;
        private double timeRemaining;

        public ActivePotion(StatPotionDefinition def) {
            this.def = def;
            this.timeRemaining = def.effectDuration();
        }

        void tick(double delta) {
            timeRemaining -= delta;
        }

        boolean isExpired() {
            return timeRemaining <= 0;
        }

        // Delegate methods for cleaner access
        String getStatType() { return def.statType(); }
        double getMultiplier() { return def.multiplier(); }
        String getName() { return def.name(); }
    }

    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.definition = new PetDefinition("Bear");

        // Initialize stats
        stats.registerStat(PetStats.STAT_HUNGER, 50);
        stats.registerStat(PetStats.STAT_HAPPINESS, 50);
        stats.registerStat(PetStats.STAT_ENERGY, 50);
        stats.registerStat(PetStats.STAT_CLEANLINESS, 50);

        // Initialize default state
        StateRegistry registry = StateRegistry.getInstance();
        PetState awakeState = registry.getState(AwakeState.STATE_NAME);
        this.currentState = new SimpleObjectProperty<>(awakeState);

        // Subscribe to environment systems
        if (clock != null) clock.subscribe(this);
        if (weatherSystem != null) weatherSystem.subscribe(this);

        // Set up daily inventory items
        replenishDailyFood();
    }

    // --- Potion Logic ---

    /**
     * Calculates the total multiplier for a specific stat based on active potions.
     * @param statName The name of the stat (e.g., PetStats.STAT_ENERGY)
     * @return The cumulative multiplier (e.g., 1.0 if none, 0.5 or 2.0 etc.)
     */
    public double getStatMultiplier(String statName) {
        double totalMultiplier = 1.0;

        for (ActivePotion potion : activePotions) {
            if (potion.getStatType().equals(statName)) {
                totalMultiplier *= potion.getMultiplier();
            }
        }
        
        return totalMultiplier;
    }

    /**
     * Returns the remaining duration of the longest active potion for a specific stat.
     * @param statName The stat identifier (e.g. PetStats.STAT_ENERGY)
     * @return Time remaining in seconds, or 0.0 if no active potion.
     */
    public double getPotionDuration(String statName) {
        double maxDuration = 0.0;
        for (ActivePotion potion : activePotions) {
            if (potion.getStatType().equals(statName)) {
                maxDuration = Math.max(maxDuration, potion.timeRemaining);
            }
        }
        return maxDuration;
    }

    public void addPotion(StatPotionDefinition def) {
        if (def != null) {
            activePotions.add(new ActivePotion(def));
            System.out.println("Effect Applied: " + def.name() + " (x" + def.multiplier() + " to " + def.statType() + ")");
        }
    }

    // --- Existing Methods ---

    public void changeState(PetState newState) {
        if (newState == null) return;
        currentState.set(newState);
        System.out.println(name + " changed state to: " + newState.getStateName());
    }

    public PetState getCurrentState() {
        return currentState.get();
    }

    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }

    public double getCurrentGameHour() {
        if (clock == null) return 0.0;
        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;
        return (normalizedTime * 24.0) % 24.0;
    }

    public boolean performConsume(Item item) {
        return currentState.get().handleConsume(this, item);
    }

    public void requestSleepInteraction() {
        currentState.get().handleSleep(this);
    }

    public void performClean() {
        currentState.get().handleClean(this);
    }

    public boolean canPlayMinigame() {
        return true; 
    }

    private MinigameResult playMinigame(Minigame minigame) {
        if (minigame == null) return null;

        MinigameResult result = minigame.play(this);

        if (result != null) {
            stats.modifyStat(PetStats.STAT_HAPPINESS, result.getHappinessDelta());
            System.out.println(result.getMessage());
        }

        return result;
    }

    public MinigameResult playRandomMinigame() {
        List<Minigame> availableGames = new ArrayList<>();
        availableGames.add(new TimingGame());
        Minigame randomGame = availableGames.get(random.nextInt(availableGames.size()));
        return playMinigame(randomGame);
    }

    public void replenishDailyFood() {
        System.out.println("Replenishing Daily Food");
        for (int i = 0; i < random.nextInt(1, 4); i++) {
            Item foodItem = ItemRegistry.get(i);
            int amount = random.nextInt(1, 4);
            inventory.add(foodItem, amount);
        }
    }

    public boolean shouldPromptSleep() {
        if (clock == null) return false;
        if (currentState.get() instanceof AsleepState || sleptThisNight) return false;

        double hour = getCurrentGameHour();

        return hour >= GameConfig.HOUR_SLEEP_WINDOW_START || hour < GameConfig.HOUR_SLEEP_WINDOW_END;
    }

    public void applyStatDecay(double timeDelta) {
        hungerDecayAccum -= definition.hungerDecayRate() * timeDelta;
        cleanlinessDecayAccum -= definition.cleanlinessDecayRate() * timeDelta;
        
        if (hungerDecayAccum <= -1.0 || hungerDecayAccum >= 1.0) {
            int hungerDelta = (int) Math.floor(hungerDecayAccum);
            hungerDecayAccum -= hungerDelta;
            stats.modifyStat(PetStats.STAT_HUNGER, hungerDelta);
        }

        if (cleanlinessDecayAccum <= -1.0 || cleanlinessDecayAccum >= 1.0) {
            int cleanDelta = (int) Math.floor(cleanlinessDecayAccum);
            cleanlinessDecayAccum -= cleanDelta;
            stats.modifyStat(PetStats.STAT_CLEANLINESS, cleanDelta);
        }

        double currentHunger = stats.getStat(PetStats.STAT_HUNGER).get();
        double currentClean = stats.getStat(PetStats.STAT_CLEANLINESS).get();

        double happinessRate = definition.happinessDecayRate();

        if (currentHunger < 30) happinessRate *= 2.0;
        if (currentClean < 30) happinessRate *= 1.5;

        happinessDecayAccum -= happinessRate * timeDelta;

        if (Math.abs(happinessDecayAccum) >= 1.0) {
            int happyDelta = (int) Math.floor(happinessDecayAccum);
            happinessDecayAccum -= happyDelta;
            stats.modifyStat(PetStats.STAT_HAPPINESS, happyDelta);
        }
    }

    @Override
    public void onTick(double timeDelta) {
        // 1. Update Potions
        for (ActivePotion potion : activePotions) {
            potion.tick(timeDelta);
            if (potion.isExpired()) {
                activePotions.remove(potion);
                System.out.println("Effect Expired: " + potion.getName());
            }
        }

        // 2. Existing State Logic
        if (currentState.get() != null) {
            currentState.get().onTick(this, timeDelta);
        }
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction
    }

    public void addToInventory(Item item, int quantity) {
        inventory.add(item, quantity);
    }
}