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
import com.eleven.pet.minigames.Minigame;
import com.eleven.pet.minigames.MinigameResult;
import com.eleven.pet.minigames.impl.TimingGame;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Core model representing the virtual sprites, its stats, state, inventory and interactions with
 * environment systems such as clock and weather.
 *
 * <p>This class is the central domain object and is responsible for:
 * <ul>
 *   <li>Holding the sprites's {@link PetStats} and {@link Inventory}.</li>
 *   <li>Managing the current {@link PetState} via the {@link StateRegistry}.</li>
 *   <li>Listening to {@link GameClock} ticks and {@link WeatherSystem} changes.</li>
 *   <li>Performing actions such as sleeping, cleaning, consuming items and playing minigames.</li>
 * </ul>
 */
@Data
public class PetModel implements TimeListener, WeatherListener {
    private static final java.util.Random random = new java.util.Random();

    /**
     * The sprites's display name.
     */
    private final String name;

    /**
     * Container for numeric stats (hunger, happiness, energy, cleanliness, etc.).
     */
    private final PetStats stats = new PetStats();

    /**
     * Property holding the current state of the sprites (awake, asleep, etc.).
     */
    private final ObjectProperty<PetState> currentState;

    /**
     * External weather system the sprites listens to for weather changes.
     */
    private final WeatherSystem weatherSystem;

    /**
     * External game clock the sprites listens to for time ticks.
     */
    private final GameClock clock;

    /**
     * The sprites's inventory.
     */
    private final Inventory inventory = new Inventory();

    /**
     * Accumulator used to aggregate fractional hunger decay until a whole point can be applied.
     */
    private double hungerDecayAccum = 0.0;

    /**
     * Accumulator used to aggregate fractional cleanliness decay until a whole point can be applied.
     */
    private double cleanlinessDecayAccum = 0.0;

    /**
     * Accumulator for happiness decay.
     */
    private double happinessDecayAccum = 0.0;

    /**
     * Flag to indicate whether the sprites has already slept during the current night cycle.
     */
    private boolean sleptThisNight = false;

    /**
     * Flag to indicate whether the clock has passed 8 AM in the current day cycle.
     */
    private boolean passedEightAM = false;

    /**
     * Tracks the duration the sprites has been sleeping during the current sleep session
     * (in game seconds).
     */
    private double currentSleepDuration = 0.0;

    /**
     * Counter used for rewards based on hours slept.
     */
    private int hoursSleptRewardCount = 0;

    
    /**
     * Definition of pet varition (species, decay rates, etc.).
     */
    private PetDefinition definition;

    /**
     * Constructs a new PetModel and subscribes it to the provided environment systems.
     *
     * @param name          The sprites's name.
     * @param weatherSystem Weather system to subscribe to; may be {@code null}.
     * @param clock         Game clock to subscribe to; may be {@code null}.
     */
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

    /**
     * Change the sprites's current state.
     *
     * @param newState New {@link PetState} to apply; ignored if {@code null}.
     */
    public void changeState(PetState newState) {
        if (newState == null) return;
        currentState.set(newState);
        System.out.println(name + " changed state to: " + newState.getStateName());
    }

    /**
     * Returns the current {@link PetState}.
     *
     * @return current state instance, or {@code null} if not set.
     */
    public PetState getCurrentState() {
        return currentState.get();
    }

    /**
     * Returns a read-only property for the current {@link PetState} to allow UI bindings.
     *
     * @return read-only state property.
     */
    public ReadOnlyObjectProperty<PetState> getStateProperty() {
        return currentState;
    }

    /**
     * Calculates the current in-game hour (0.0 - &lt;24.0).
     *
     * @return current game hour or {@code 0.0} if clock is not available.
     */
    public double getCurrentGameHour() {
        if (clock == null) return 0.0;
        double gameTime = clock.getGameTime();
        double normalizedTime = gameTime / GameConfig.DAY_LENGTH_SECONDS;
        return (normalizedTime * 24.0) % 24.0;
    }

    /**
     * Attempt to consume the given {@link Item} via the current state handler.
     *
     * @param item item to consume.
     * @return {@code true} if the consume action was accepted/handled; {@code false} otherwise.
     */
    public boolean performConsume(Item item) {
        return currentState.get().handleConsume(this, item);
    }

    /**
     * Request the sprites to attempt to sleep via the current state's sleep handler.
     */
    public void requestSleepInteraction() {
        currentState.get().handleSleep(this);
    }

    /**
     * Request the sprites to be cleaned via the current state's clean handler.
     */
    public void performClean() {
        currentState.get().handleClean(this);
    }

    /**
     * Check whether the sprites is eligible to play a minigame.
     *
     * @return {@code true} if a minigame can be started; {@code false} otherwise.
     */
    public boolean canPlayMinigame() {
        // TODO: Implement minigame eligibility check
        return true; // For now, always allow minigames
    }

    /**
     * Play the provided {@link Minigame} and apply its effects to the sprites (e.g. happiness).
     *
     * @param minigame minigame to play.
     * @return the {@link MinigameResult} returned by the minigame, or {@code null} if none.
     */
    private MinigameResult playMinigame(Minigame minigame) {
        if (minigame == null) return null;

        MinigameResult result = minigame.play(this);

        if (result != null) {
            // Apply happiness delta from the minigame result
            stats.modifyStat(PetStats.STAT_HAPPINESS, result.getHappinessDelta());
            System.out.println(result.getMessage());
        }

        return result;
    }

    /**
     * Selects and plays a random minigame from a local list of implementations.
     *
     * @return result of the played minigame, or {@code null} if nothing was played.
     */
    public MinigameResult playRandomMinigame() {
        // TODO: Delegate this behavior to the PetState
        // TODO: Use autoservice to populate the minigame list

        // Create list of available minigames
        List<Minigame> availableGames = new ArrayList<>();
        availableGames.add(new TimingGame());

        // Pick a random minigame
        Minigame randomGame = availableGames.get(random.nextInt(availableGames.size()));

        return playMinigame(randomGame);
    }

    /**
     * Replenish the sprites's daily food items into its inventory.
     */
    public void replenishDailyFood() {
        // TODO: Fix this logic to be more dynamic
        System.out.println("Replenishing Daily Food");
        for (int i = 0; i < random.nextInt(1, 4); i++) {
            Item foodItem = ItemRegistry.get(i);
            int amount = random.nextInt(1, 4);
            inventory.add(foodItem, amount);
        }
    }

    /**
     * Determine whether the sprites should be prompted to sleep based on the current
     * game hour, state, and flags.
     *
     * @return {@code true} when the sleep prompt should be shown.
     */
    public boolean shouldPromptSleep() {
        if (clock == null) return false;
        if (currentState.get() instanceof AsleepState || sleptThisNight) return false;

        double hour = getCurrentGameHour();

        return hour >= GameConfig.HOUR_SLEEP_WINDOW_START || hour < GameConfig.HOUR_SLEEP_WINDOW_END;
    }

    /**
     * Apply stat decay over the given time delta. This method accumulates fractional decay
     * amounts and applies whole integer deltas to the underlying stats.
     *
     * @param timeDelta elapsed time in seconds since the last tick.
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

        //stats.calculateDerivedHappiness();
        double currentHunger = stats.getStat(PetStats.STAT_HUNGER).get();
        double currentClean = stats.getStat(PetStats.STAT_CLEANLINESS).get();

        // Base boredom decay
        double happinessRate = definition.happinessDecayRate();

        // Penalty multipliers: If stats are critical (< 30), happiness drops 2x or 3x faster
        if (currentHunger < 30) happinessRate *= 2.0;
        if (currentClean < 30) happinessRate *= 1.5;

        happinessDecayAccum -= happinessRate * timeDelta;

        if (Math.abs(happinessDecayAccum) >= 1.0) {
            int happyDelta = (int) Math.floor(happinessDecayAccum);
            happinessDecayAccum -= happyDelta;
            stats.modifyStat(PetStats.STAT_HAPPINESS, happyDelta);
        }
    }

    /**
     * Called each game tick by the {@link GameClock}. Delegates to the current {@link PetState}.
     *
     * @param timeDelta elapsed time in seconds since last tick.
     */
    @Override
    public void onTick(double timeDelta) {
        if (currentState.get() != null) {
            currentState.get().onTick(this, timeDelta);
        }
    }

    /**
     * Called when the {@link WeatherSystem} reports a weather change. Intended to allow states
     * or stats to react (e.g. happiness changes).
     *
     * @param newWeather new weather state.
     */
    @Override
    public void onWeatherChange(WeatherState newWeather) {
        // TODO: Implement weather change reaction (modify happiness based on weather)
    }
}