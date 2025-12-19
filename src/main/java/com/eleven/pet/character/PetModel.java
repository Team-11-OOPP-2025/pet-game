package com.eleven.pet.character;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.eleven.pet.character.behavior.AwakeState;
import com.eleven.pet.character.behavior.PetState;
import com.eleven.pet.character.behavior.StateRegistry;
import com.eleven.pet.core.AssetLoader;
import com.eleven.pet.core.GameConfig;
import com.eleven.pet.environment.time.GameClock;
import com.eleven.pet.environment.time.TimeListener;
import com.eleven.pet.environment.weather.WeatherListener;
import com.eleven.pet.environment.weather.WeatherState;
import com.eleven.pet.environment.weather.WeatherSystem;
import com.eleven.pet.inventory.ActivePotion;
import com.eleven.pet.inventory.Inventory;
import com.eleven.pet.inventory.Item;
import com.eleven.pet.inventory.ItemRegistry;
import com.eleven.pet.inventory.StatPotionDefinition;
import com.eleven.pet.minigames.MinigameResult;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;

/**
 * Core domain model for a single pet instance.
 *
 * <p>Tracks stats, state machine, inventory, potions, and reacts to
 * environment updates such as time and weather. Exposes a public API
 * used by UI and game logic to interact with the pet.</p>
 */
@Data
public class PetModel implements TimeListener, WeatherListener {
    private static final Random random = new Random();

    private final String name;
    private final PetStats stats = new PetStats();
    private final ObjectProperty<PetState> currentState;
    private final WeatherSystem weatherSystem;
    private final GameClock clock;
    private boolean tutorialCompleted;

    private final Inventory inventory = new Inventory();

    private double hungerDecayAccum = 0.0;
    private double cleanlinessDecayAccum = 0.0;
    private double happinessDecayAccum = 0.0;

    private boolean sleptThisNight = false;
    private boolean passedEightAM = false;
    private double currentSleepDuration = 0.0;
    private int hoursSleptRewardCount = 0;

    // Remaining time (in game hours) until the next reward can be claimed
    private double rewardCooldown = 0.0;

    private PetDefinition definition;

    // Thread-safe list to handle concurrent modifications during ticks
    private final List<ActivePotion> activePotions = new CopyOnWriteArrayList<>();

    /**
     * Creates a new pet model with default stats, behavior, and definition.
     *
     * <p>Subscribes to the provided {@link GameClock} and {@link WeatherSystem}
     * if they are non-null and initializes daily inventory items.</p>
     *
     * @param name          pet name
     * @param weatherSystem weather system to subscribe to, may be {@code null}
     * @param clock         game clock to subscribe to, may be {@code null}
     */
    public PetModel(String name, WeatherSystem weatherSystem, GameClock clock) {
        this.name = name;
        this.weatherSystem = weatherSystem;
        this.clock = clock;
        this.definition = new PetDefinition("Bear"); // Default definition

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

    /**
     * Computes the effective multiplier for a given stat based on all active potions.
     *
     * @param statName name of the stat (e.g. {@link PetStats#STAT_HUNGER})
     * @return product of all active multipliers affecting that stat, or {@code 1.0} if none
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
     * Returns the remaining duration of the longest potion affecting a given stat.
     *
     * @param statName name of the stat
     * @return remaining time in game hours of the strongest-duration potion, or {@code 0.0} if none
     */
    public double getPotionDuration(String statName) {
        double maxDuration = 0.0;
        for (ActivePotion potion : activePotions) {
            if (potion.getStatType().equals(statName)) {
                maxDuration = Math.max(maxDuration, potion.getTimeRemaining());
            }
        }
        return maxDuration;
    }

    /**
     * Adds a new active potion effect to the pet.
     *
     * @param def definition of the potion; ignored if {@code null}
     */
    public void addPotion(StatPotionDefinition def) {
        if (def != null) {
            activePotions.add(new ActivePotion(def));
            System.out.println("Effect Applied: " + def.name() + " (x" + def.multiplier() + " to " + def.statType() + ")");
        }
    }

    /**
     * Change the current behavioral state of the pet.
     *
     * @param newState new state to switch to; if {@code null}, the call is ignored
     */
    public void changeState(PetState newState) {
        if (newState == null) return;
        currentState.set(newState);
        System.out.println(name + " changed state to: " + newState.getStateName());
        String soundName = newState.getSoundName();
        if (soundName != null) {
        AssetLoader.getInstance().playSound(soundName);
    }
    }

    /**
     * Get the current behavioral state.
     *
     * @return current {@link PetState}, or {@code null} if not initialized
     */
    public PetState getCurrentState() {
        return currentState.get();
    }

    /**
     * Returns an observable property for the pet's current state.
     *
     * @return read-only object property of {@link PetState}
     */
    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }

    /**
     * Returns the current in-game time as an hour-of-day value.
     *
     * @return hour in range [0, 24), or {@code 0.0} if no clock is attached
     */
    public double getCurrentGameHour() {
        if (clock == null) return 0.0;
        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;
        return (normalizedTime * 24.0) % 24.0;
    }

    /**
     * Requests the current state to handle a consume action with the given item.
     *
     * @param item item to consume
     * @return {@code true} if the consume action was performed, {@code false} otherwise
     */
    public boolean performConsume(Item item) {
        return currentState.get().handleConsume(this, item);
    }

    /**
     * Requests the current state to handle a sleep interaction.
     */
    public void requestSleepInteraction() {
        currentState.get().handleSleep(this);
    }

    /**
     * Requests the current state to handle a cleaning interaction.
     */
    public void performClean() {
        currentState.get().handleClean(this);
    }

    /**
     * Feeds the pet, increasing hunger by the specified amount.
     * Centralizes the business logic for eating.
     *
     * @param hungerRestored amount of hunger to restore
     * @return {@code true} if the pet was successfully fed
     */
    public boolean eat(int hungerRestored) {
        if (!stats.hasStat(PetStats.STAT_HUNGER)) {
            return false;
        }
        stats.modifyStat(PetStats.STAT_HUNGER, hungerRestored);
        return true;
    }

    /**
     * Applies penalties for missing sleep overnight.
     * Centralizes the business logic for missed sleep penalties.
     */
    public void applyMissedSleepPenalty() {
        System.out.println(name + " stayed up all night! Penalty applied.");
        stats.modifyStat(PetStats.STAT_ENERGY, -GameConfig.MISSED_SLEEP_ENERGY_PENALTY);
        stats.modifyStat(PetStats.STAT_HAPPINESS, -GameConfig.MISSED_SLEEP_HAPPINESS_PENALTY);
    }

    public boolean canPlayMinigame() {
        return currentState.get().canPlay(this);
    }

    /**
     * Replenishes daily food items into the pet's inventory.
     *
     * <p>The amount and type of food are randomized within configured bounds.</p>
     */
    public void replenishDailyFood() {
        System.out.println("Replenishing Daily Food");
        for (int i = 0; i < random.nextInt(1, 6); i++) {
            Item foodItem = ItemRegistry.get(i);
            int amount = random.nextInt(1, 4);
            inventory.add(foodItem, amount);
        }
    }

    /**
     * Determines whether the UI should prompt the player to let the pet sleep.
     *
     * @return {@code true} if the pet is awake, has not slept this night, and is within the sleep window
     */
    public boolean shouldPromptSleep() {
        if (clock == null) return false;
        if (!currentState.get().canSleep()) return false;

        double hour = getCurrentGameHour();
        return hour >= GameConfig.HOUR_SLEEP_WINDOW_START || hour < GameConfig.HOUR_SLEEP_WINDOW_END;
    }

    /**
     * Applies stat decay over a given time delta.
     *
     * <p>Uses the {@link PetDefinition} rates to reduce hunger, cleanliness, and happiness
     * over time, with additional penalties when hunger or cleanliness are low.</p>
     *
     * @param timeDelta elapsed time in game hours
     */
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

        double happinessRate = (definition != null) ? definition.happinessDecayRate() : GameConfig.HAPPINESS_DECAY_RATE;

        if (currentHunger < 30) happinessRate *= 2.0;
        if (currentClean < 30) happinessRate *= 1.5;
        
        // Apply weather happiness modifier
        if (weatherSystem != null && weatherSystem.getCurrentWeather() != null) {
            double weatherModifier = weatherSystem.getCurrentWeather().getHappinessModifier();
            happinessRate /= weatherModifier; // Higher modifier = slower decay 
        }

        happinessDecayAccum -= happinessRate * timeDelta;

        if (Math.abs(happinessDecayAccum) >= 1.0) {
            int happyDelta = (int) Math.floor(happinessDecayAccum);
            happinessDecayAccum -= happyDelta;
            stats.modifyStat(PetStats.STAT_HAPPINESS, happyDelta);
        }
    }

    /**
     * Applies the result of a minigame to the pet's stats.
     *
     * @param result result of the minigame played
     */
    public void applyMinigameResult(MinigameResult result) {
        if (result == null) return;
        stats.modifyStat(PetStats.STAT_HAPPINESS, result.happinessDelta());
    }

    /**
     * Adds the given item and quantity to the pet's inventory.
     *
     * @param item     item to add
     * @param quantity number of units to add
     */
    public void addToInventory(Item item, int quantity) {
        inventory.add(item, quantity);
    }

    /**
     * Returns a read-only property for the given stat.
     *
     * @param statName name of the stat (e.g. {@link PetStats#STAT_HAPPINESS})
     * @return read-only integer property, or {@code null} if the stat does not exist
     */
    public ReadOnlyIntegerProperty getStatProperty(String statName) {
        return stats.getStat(statName);
    }

    /**
     * Called each tick by the {@link GameClock}.
     *
     * <p>Updates potion durations, reward cooldown, and delegates to the current state.</p>
     *
     * @param timeDelta elapsed in-game time since the last tick, in hours
     */
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

        // 2. Update Reward Cooldown
        if (clock != null && rewardCooldown > 0) {
            // timeDelta is scaled by GameClock; 1 unit = 1 in-game hour.
            rewardCooldown -= timeDelta;
            if (rewardCooldown < 0) {
                rewardCooldown = 0;
            }
        }

        // 3. Existing State Logic
        if (currentState.get() != null) {
            currentState.get().onTick(this, timeDelta);
        }
    }

    @Override
    public void onWeatherChange(WeatherState newWeather) {
        System.out.println(name + " notices the weather changed to: " + newWeather.getName());
        // Weather effects are applied continuously through the happiness decay modifier
    }
}