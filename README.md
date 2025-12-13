# Virtual Pet Evolution (Björni)

## 🐻 Meet Björni

Björni is more than just a collection of pixels; he is a fully simulated digital companion designed to live on your desktop. Built with a focus on autonomous behavior and environmental awareness, Björni mimics the lifecycle of a real pet through advanced state management and environmental simulation.

Unlike static virtual pets that wait for user input, Björni lives in a breathing world:

- **He follows the Sun**: Driven by an internal `GameClock`, Björni knows when it is day or night. When the sun sets, his energy drains faster, and he will eventually need to sleep to recover.
- **He feels the Weather**: The `WeatherSystem` determines if it is raining, sunny, or cloudy. Björni's happiness fluctuates based on these conditions—he prefers sunny days but might need extra attention during a storm.
- **He has Complex Needs**: Björni is governed by a strict state machine. You cannot force him to play when he is asleep, and you cannot feed him if he is too full. You must learn his schedule to keep him happy.

### The Björni Personality

- **Species**: Digital Brown Bear (*Ursus digitalis*)
- **Favorite Weather**: Sunshine (High Happiness Modifier)
- **Sleep Schedule**: Strictly nocturnal (sleeps when `DayCycle` = NIGHT)
- **Temperament**: Generally happy, but will refuse to interact if his Energy drops below critical levels.

> **💡 Implementation Tip: "The Hibernation Feature"** > When the user closes the application, Björni enters Hibernation Mode (serialization). The game uses AES-GCM encryption to secure his "cave" (the save file), ensuring that his state is perfectly preserved until he wakes up again.

## Project Overview

Virtual Pet Evolution is a robust, model-heavy desktop simulation game built with **Java 23** and **JavaFX**. Far from a simple toy application, it uses a **Package-by-Feature** architecture to organize code by domain (Character, Environment, Storage) rather than technical layer.

The project demonstrates advanced software engineering principles, including:

- **Reactive MVC Architecture**: A decomposed View layer that observes the Model via JavaFX Properties.
- **Cryptographic Persistence**: Secure state saving with AES-GCM.
- **Decoupled Game Loop**: Separation of simulation ticks from rendering frames.
- **State-Driven Behavior**: A Finite State Machine (FSM) enforcing game rules.

## Features

- **Living Desktop Pet**: Interact with Björni by feeding, playing, cleaning, and letting him sleep.
- **Embedded Minigame System**: Click the in-game TV to zoom in and play fully interactive minigames (like *Guessing Game* or *Timing Challenge*) directly on the virtual screen.
- **Inventory System**: Collect food items (Apples, Pears, etc.) and manage them via a reactive Inventory UI.
- **Dynamic Day/Night Cycle**: A `GameClock` drives visual transitions (Day/Night backgrounds) and affects stat decay rates.
- **Reactive Weather System**: Cycles between Sunny, Rainy, and Cloudy states, triggering particle effects (rain) and happiness modifiers.
- **Needs & Stats System**: Hunger, Happiness, Energy, and Cleanliness are tracked and decay over time based on `GameConfig`.
- **Secure Save System**: Game state is serialized into a `PetDataDTO` and encrypted using `GcmEncryptionService`.
- **Autosave**: Background routines ensure Björni's progress is never lost.

## Architectural Overview

The project follows a modular architecture ensuring high cohesion and low coupling.

### 1. Core Engine (`com.eleven.pet.core`)
The "Engine Room" of the application.
- **MainApp**: Entry point handling Dependency Injection (wiring) and window setup.
- **GameEngine**: Encapsulates the `AnimationTimer` "Game Loop," ticking systems 60 times/sec.
- **AssetLoader**: Singleton that caches images and loads custom fonts (e.g., Minecraft/Pixel style).

### 2. UI Layer (`com.eleven.pet.ui` & subpackages)
The project uses a **Reactive MVC** pattern where Views observe the Model directly but send actions to the Controller. The UI is decomposed into specialized components:
- **WorldView**: Manages the environment, backgrounds, weather particle effects, and the interactive TV area.
- **HUDView**: Handles status bars (Hunger, Happiness) and main action buttons. It communicates with the Controller to toggle other UI states (like the Inventory).
- **InventoryView**: A reactive grid displaying `Item` stacks. It listens to the `PetController` for visibility toggles.
- **PetAvatarView**: Dedicated to rendering the character. It uses `SpriteSheetAnimation` to switch visuals based on the pet's emotional state (Happy, Sad, Sleeping).

### 3. Character Feature (`com.eleven.pet.character`)
**Model ("The Brain")**:
- **PetModel**: The domain root. Holds `PetStats`, `Inventory`, and the current `PetState`. It adheres to the **Open/Closed Principle** by exposing a generic `getStatProperty(name)` for UI binding.
- **behavior.***: The State Machine logic (`AwakeState`, `AsleepState`).

**Controller ("The Orchestrator")**:
- **PetController**: Coordinates user inputs, manages UI state (like `inventoryOpen`), and bridges the Model with Persistence and Minigames.

### 4. Minigames (`com.eleven.pet.minigames`)
A pluggable system for in-game activities.
- **Minigame Interface**: Defines how a game starts and provides its own JavaFX `Pane`.
- **Embedded Architecture**: Games run inside the `WorldView`'s TV pane rather than separate windows, maintaining immersion.

### 5. Environment Systems (`com.eleven.pet.environment`)
- **Time**: `GameClock` and `DayCycle` manage time flow.
- **Weather**: `WeatherSystem` switches strategies (`SunnyState`, `RainyState`) to apply global modifiers.

## Key Design Patterns

### Model-View-Controller (MVC)
The application strictly separates data (Model), visualization (View), and logic (Controller). Views use the **Observer Pattern** to reactively update when the Model changes (e.g., health bar shrinking).

### State Pattern
- **PetState** defines behaviors like `handleFeed` or `handleSleep`.
- **StateRegistry** allows dynamic lookup of states.

### Strategy Pattern
- Used in **WeatherSystem** (`SunnyState`, `RainyState`) to swap environmental rules at runtime.
- Used in **Minigames** to allow the Controller to plug different games into the TV view.

### Singleton Pattern
- **AssetLoader** provides centralized, cached access to heavy resources like images and fonts.

## Technical Stack

- **Language**: Java 23
- **UI Framework**: JavaFX (Canvas for particles, Scene Graph for UI)
- **Persistence**: Jackson (JSON) + `javax.crypto` (AES-GCM)
- **Concurrency**: `javafx.animation.AnimationTimer` (Game Loop), `Timeline` (Events)

## Setup & Run

### Prerequisites
- JDK 23+ installed.
- Maven installed.

### Run with Maven
```bash
mvn clean javafx:run
```

On first launch, the game generates a cryptographic key for the secure save file.

## Design & Architecture Deep Dive

The heart of the simulation is the **PetModel**. Instead of scattering rules across the UI, all game rules—such as when Björni can be fed or how weather affects him—are encapsulated in the model.

**Extensibility via OCP**: The `PetModel` and UI bindings are designed to be Open for Extension. Adding a new stat (e.g., "Thirst") requires no changes to the binding logic in `HUDView` or `PetModel` structure, thanks to the generic property accessor pattern.

**Minigame Integration**: The Minigame system allows for infinite expandability. New games can be added by implementing the `Minigame` interface and registering them in the Controller. They automatically inherit the "TV Mode" zoom and display logic without touching the core rendering code.

## Authors

  - Filip Helin ([@Cleanmain](https://github.com/Cleanmain))
  - Hugo Dörrich ([@hugodor2005](https://github.com/hugodor2005))
  - Melwin Heimby ([@heim1024](https://github.com/heim1024))
  - Raghib Hussain ([@Anajrim01](https://github.com/Anajrim01))

## License

This project is licensed under the GNU General Public License v3.0 (GPL‑3.0) - see the [LICENSE](https://github.com/Team-11-OOPP-2025/pet-game/blob/main/LICENSE) file for details.
