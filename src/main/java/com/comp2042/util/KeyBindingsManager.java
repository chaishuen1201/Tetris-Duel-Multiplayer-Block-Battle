package com.comp2042.util;

import javafx.scene.input.KeyCode;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class KeyBindingsManager {
    private static final String PROPERTIES_FILE = "keybindings.properties";
    private static KeyBindingsManager instance;
    private Properties properties;
    private Map<String, KeyCode> keyBindings;

    public enum PlayerMode {
        SINGLE("single"),
        PLAYER1("player1"),
        PLAYER2("player2");

        private final String prefix;

        PlayerMode(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }
    }

    public enum Action {
        LEFT("LEFT"),
        RIGHT("RIGHT"),
        ROTATE("ROTATE"),
        SOFT_DROP("SOFT_DROP"),
        HARD_DROP("HARD_DROP"),
        HOLD("HOLD"),
        PAUSE("PAUSE"),
        NEW_GAME("NEW_GAME");

        private final String name;

        Action(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private KeyBindingsManager() {
        properties = new Properties();
        keyBindings = new HashMap<>();
        loadKeyBindings();
    }

    public static KeyBindingsManager getInstance() {
        if (instance == null) {
            instance = new KeyBindingsManager();
        }
        return instance;
    }

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

    public void saveKeyBindings() {
        try (OutputStream output = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(output, "Key Bindings Configuration");
        } catch (IOException e) {
            System.err.println("Error saving key bindings: " + e.getMessage());
        }
    }

    public void setKeyBinding(PlayerMode mode, Action action, KeyCode keyCode) {
        String key = mode.getPrefix() + "." + action.getName();
        keyBindings.put(key, keyCode);
        properties.setProperty(key, keyCode.name());
    }

    public KeyCode getKeyBinding(PlayerMode mode, Action action) {
        String key = mode.getPrefix() + "." + action.getName();
        return keyBindings.getOrDefault(key, null);
    }

    public boolean isKeyBound(KeyCode keyCode, PlayerMode mode) {
        for (Action action : Action.values()) {
            KeyCode bound = getKeyBinding(mode, action);
            if (bound == keyCode) {
                return true;
            }
        }
        return false;
    }

    public Action getActionForKey(KeyCode keyCode, PlayerMode mode) {
        for (Action action : Action.values()) {
            KeyCode bound = getKeyBinding(mode, action);
            if (bound == keyCode) {
                return action;
            }
        }
        return null;
    }

    public String getKeyBindingDisplay(PlayerMode mode, Action action) {
        KeyCode keyCode = getKeyBinding(mode, action);
        if (keyCode == null) {
            return "Not Set";
        }
        return formatKeyCode(keyCode);
    }

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

