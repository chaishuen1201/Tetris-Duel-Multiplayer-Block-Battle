package com.comp2042.util;

import javafx.scene.input.KeyCode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Manages keyboard key bindings for game controls using the Singleton pattern.
 * This class handles all keyboard input configuration for the game, supporting both
 * single player and multiplayer modes with separate key bindings for each player.
 * It loads and saves key bindings from a properties file (keybindings.properties),
 * allowing users to customize their controls. The manager provides methods to get,
 * set, and check key bindings for different game actions (move left/right, rotate,
 * soft drop, hard drop, hold, pause, new game). It also handles conflict detection
 * between player modes and provides formatted display strings for key codes.
 * Default bindings are automatically initialized if the properties file doesn't exist.
 */
public class KeyBindingsManager {
    private static final String PROPERTIES_FILE = "keybindings.properties";
    private static KeyBindingsManager instance;
    private Properties properties;
    private Map<String, KeyCode> keyBindings;

    /**
     * Enumeration representing different player modes for key bindings.
     * SINGLE represents single player mode, PLAYER1 and PLAYER2 represent
     * the two players in multiplayer mode. Each mode has its own set of
     * key bindings stored with a unique prefix in the properties file.
     */
    public enum PlayerMode {
        /** Single player mode key bindings */
        SINGLE("single"),
        /** Player 1 key bindings in multiplayer mode */
        PLAYER1("player1"),
        /** Player 2 key bindings in multiplayer mode */
        PLAYER2("player2");

        private final String prefix;

        /**
         * Creates a PlayerMode with the specified prefix for properties file storage.
         * 
         * @param prefix The prefix string used in the properties file for this mode
         */
        PlayerMode(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Gets the prefix string for this player mode.
         * 
         * @return The prefix string used in the properties file
         */
        public String getPrefix() {
            return prefix;
        }
    }

    /**
     * Enumeration representing all possible game actions that can be bound to keys.
     * Each action represents a specific game control that can be triggered by
     * pressing a keyboard key.
     */
    public enum Action {
        /** Move the current piece left */
        LEFT("LEFT"),
        /** Move the current piece right */
        RIGHT("RIGHT"),
        /** Rotate the current piece counter-clockwise */
        ROTATE("ROTATE"),
        /** Soft drop - move the piece down faster */
        SOFT_DROP("SOFT_DROP"),
        /** Hard drop - instantly drop the piece to the bottom */
        HARD_DROP("HARD_DROP"),
        /** Hold the current piece and swap with held piece */
        HOLD("HOLD"),
        /** Pause/unpause the game */
        PAUSE("PAUSE"),
        /** Start a new game */
        NEW_GAME("NEW_GAME");

        private final String name;

        /**
         * Creates an Action with the specified name.
         * 
         * @param name The name string used in the properties file for this action
         */
        Action(String name) {
            this.name = name;
        }

        /**
         * Gets the name string for this action.
         * 
         * @return The name string used in the properties file
         */
        public String getName() {
            return name;
        }
    }

    /**
     * Private constructor for Singleton pattern.
     * Initializes properties and key bindings map, then loads key bindings
     * from the properties file or initializes defaults if the file doesn't exist.
     */
    private KeyBindingsManager() {
        properties = new Properties();
        keyBindings = new HashMap<>();
        loadKeyBindings();
    }

    /**
     * Gets the singleton instance of KeyBindingsManager.
     * Creates a new instance if one doesn't exist (lazy initialization).
     * 
     * @return The singleton KeyBindingsManager instance
     */
    public static KeyBindingsManager getInstance() {
        if (instance == null) {
            instance = new KeyBindingsManager();
        }
        return instance;
    }

    /**
     * Loads key bindings from the properties file.
     * If the file doesn't exist, initializes default bindings and saves them.
     * If there's an error reading the file, falls back to default bindings.
     * Invalid key codes in the file are logged as errors but don't stop loading.
     */
    public void loadKeyBindings() {
        try (InputStream input = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(input);
            keyBindings.clear();
            
            // Load all key bindings
            for (PlayerMode mode : PlayerMode.values()) {
                for (Action action : Action.values()) {
                    String key = mode.getPrefix() + "." + action.getName();
                    String value = properties.getProperty(key);
                    if (value != null) {
                        try {
                            KeyCode keyCode = KeyCode.valueOf(value);
                            keyBindings.put(key, keyCode);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid key code: " + value + " for " + key);
                        }
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, use defaults
            initializeDefaults();
            saveKeyBindings();
        } catch (IOException e) {
            System.err.println("Error loading key bindings: " + e.getMessage());
            initializeDefaults();
        }
    }

    /**
     * Initializes default key bindings for all player modes.
     * Single player uses arrow keys and standard controls. Player 1 uses WASD controls.
     * Player 2 uses arrow keys and Enter/Ctrl. These defaults are used when the
     * properties file doesn't exist or when there's an error loading it.
     */
    private void initializeDefaults() {
        // Single player defaults
        setKeyBinding(PlayerMode.SINGLE, Action.LEFT, KeyCode.LEFT);
        setKeyBinding(PlayerMode.SINGLE, Action.RIGHT, KeyCode.RIGHT);
        setKeyBinding(PlayerMode.SINGLE, Action.ROTATE, KeyCode.UP);
        setKeyBinding(PlayerMode.SINGLE, Action.SOFT_DROP, KeyCode.DOWN);
        setKeyBinding(PlayerMode.SINGLE, Action.HARD_DROP, KeyCode.SPACE);
        setKeyBinding(PlayerMode.SINGLE, Action.HOLD, KeyCode.C);
        setKeyBinding(PlayerMode.SINGLE, Action.PAUSE, KeyCode.P);
        setKeyBinding(PlayerMode.SINGLE, Action.NEW_GAME, KeyCode.N);

        // Player 1 defaults
        setKeyBinding(PlayerMode.PLAYER1, Action.LEFT, KeyCode.A);
        setKeyBinding(PlayerMode.PLAYER1, Action.RIGHT, KeyCode.D);
        setKeyBinding(PlayerMode.PLAYER1, Action.ROTATE, KeyCode.W);
        setKeyBinding(PlayerMode.PLAYER1, Action.SOFT_DROP, KeyCode.S);
        setKeyBinding(PlayerMode.PLAYER1, Action.HARD_DROP, KeyCode.SPACE);
        setKeyBinding(PlayerMode.PLAYER1, Action.HOLD, KeyCode.C);

        // Player 2 defaults
        setKeyBinding(PlayerMode.PLAYER2, Action.LEFT, KeyCode.LEFT);
        setKeyBinding(PlayerMode.PLAYER2, Action.RIGHT, KeyCode.RIGHT);
        setKeyBinding(PlayerMode.PLAYER2, Action.ROTATE, KeyCode.UP);
        setKeyBinding(PlayerMode.PLAYER2, Action.SOFT_DROP, KeyCode.DOWN);
        setKeyBinding(PlayerMode.PLAYER2, Action.HARD_DROP, KeyCode.ENTER);
        setKeyBinding(PlayerMode.PLAYER2, Action.HOLD, KeyCode.CONTROL);
    }

    /**
     * Saves the current key bindings to the properties file.
     * Writes all key bindings to keybindings.properties. If there's an error
     * writing the file, an error message is printed to stderr.
     */
    public void saveKeyBindings() {
        try (OutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(output, "Key Bindings Configuration");
        } catch (IOException e) {
            System.err.println("Error saving key bindings: " + e.getMessage());
        }
    }

    /**
     * Sets a key binding for a specific action in a specific player mode.
     * Updates both the in-memory map and the properties object. The binding
     * is saved to the properties file when saveKeyBindings() is called.
     * 
     * @param mode The player mode (SINGLE, PLAYER1, or PLAYER2)
     * @param action The game action to bind
     * @param keyCode The KeyCode to bind to the action
     */
    public void setKeyBinding(PlayerMode mode, Action action, KeyCode keyCode) {
        String key = mode.getPrefix() + "." + action.getName();
        keyBindings.put(key, keyCode);
        properties.setProperty(key, keyCode.name());
    }

    /**
     * Gets the key code bound to a specific action in a specific player mode.
     * 
     * @param mode The player mode (SINGLE, PLAYER1, or PLAYER2)
     * @param action The game action to look up
     * @return The KeyCode bound to the action, or null if not bound
     */
    public KeyCode getKeyBinding(PlayerMode mode, Action action) {
        String key = mode.getPrefix() + "." + action.getName();
        return keyBindings.getOrDefault(key, null);
    }

    /**
     * Checks if a key code is bound to any action in a specific player mode.
     * 
     * @param keyCode The KeyCode to check
     * @param mode The player mode to check in
     * @return true if the key is bound to any action in the specified mode, false otherwise
     */
    public boolean isKeyBound(KeyCode keyCode, PlayerMode mode) {
        for (Action action : Action.values()) {
            KeyCode bound = getKeyBinding(mode, action);
            if (bound == keyCode) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the action that a key code is bound to in a specific player mode.
     * 
     * @param keyCode The KeyCode to look up
     * @param mode The player mode to check in
     * @return The Action bound to the key code, or null if not bound to any action
     */
    public Action getActionForKey(KeyCode keyCode, PlayerMode mode) {
        for (Action action : Action.values()) {
            KeyCode bound = getKeyBinding(mode, action);
            if (bound == keyCode) {
                return action;
            }
        }
        return null;
    }

    /**
     * Checks if a key is already bound to another player mode.
     * Only checks between PLAYER1 and PLAYER2 (not SINGLE player).
     * 
     * @param keyCode The key code to check
     * @param currentMode The current player mode trying to bind the key
     * @return The player mode that already has this key bound, or null if no conflict
     */
    public PlayerMode getConflictingPlayerMode(KeyCode keyCode, PlayerMode currentMode) {
        // Only check conflicts between PLAYER1 and PLAYER2
        if (currentMode == PlayerMode.PLAYER1) {
            if (isKeyBound(keyCode, PlayerMode.PLAYER2)) {
                return PlayerMode.PLAYER2;
            }
        } else if (currentMode == PlayerMode.PLAYER2) {
            if (isKeyBound(keyCode, PlayerMode.PLAYER1)) {
                return PlayerMode.PLAYER1;
            }
        }
        return null;
    }

    /**
     * Gets the action that a key is bound to in another player mode.
     * 
     * @param keyCode The key code to check
     * @param conflictingMode The player mode that has the key bound
     * @return The action the key is bound to in the conflicting mode
     */
    public Action getActionInPlayerMode(KeyCode keyCode, PlayerMode conflictingMode) {
        return getActionForKey(keyCode, conflictingMode);
    }

    /**
     * Gets a formatted display string for a key binding.
     * Returns "Not Set" if the action is not bound, otherwise returns a
     * formatted version of the key code name (e.g., "5" instead of "DIGIT5").
     * 
     * @param mode The player mode (SINGLE, PLAYER1, or PLAYER2)
     * @param action The game action to get the display string for
     * @return A formatted string representation of the key binding, or "Not Set" if not bound
     */
    public String getKeyBindingDisplay(PlayerMode mode, Action action) {
        KeyCode keyCode = getKeyBinding(mode, action);
        if (keyCode == null) {
            return "Not Set";
        }
        return formatKeyCode(keyCode);
    }

    /**
     * Formats a KeyCode name for better display in the UI.
     * Removes "DIGIT" prefix from number keys and adds "Num" prefix to numpad keys.
     * 
     * @param keyCode The KeyCode to format
     * @return A formatted string representation of the key code
     */
    private String formatKeyCode(KeyCode keyCode) {
        String name = keyCode.name();
        // Format common keys for better display
        if (name.startsWith("DIGIT")) {
            return name.substring(5); // Remove "DIGIT" prefix
        } else if (name.startsWith("NUMPAD")) {
            return "Num " + name.substring(6);
        }
        return name;
    }
}

