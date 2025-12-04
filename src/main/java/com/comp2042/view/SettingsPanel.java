package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.ObjectProperty;
import com.comp2042.util.KeyBindingsManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Settings panel component for game configuration.
 * This JavaFX BorderPane component provides a comprehensive settings interface
 * for configuring game options including volume control (slider and mute button),
 * ghost piece visibility toggle, and key rebinding for all game actions. The panel
 * supports both single player and multiplayer modes with separate key bindings for
 * each player. Key rebinding features real-time visual feedback, conflict detection
 * between players, automatic key swapping when conflicts occur within the same mode,
 * and formatted key code display. The panel uses CSS styling classes for theming,
 * supports audio feedback for button interactions, and includes a scrollable controls
 * section for managing multiple key bindings. The panel is focusable to receive key
 * events during rebinding operations.
 */
public class SettingsPanel extends BorderPane {
    
    private AudioManager audioManager;

    private Slider volumeSlider;
    private Button muteButton;
    private CheckBox ghostPieceCheckBox;
    private Button backButton;
    private ScrollPane controlsScrollPane;
    private VBox controlsContainer;
    private Map<String, Button> rebindButtons = new HashMap<>();
    private Button currentRebindingButton;
    private Runnable onBackAction;
    private KeyBindingsManager keyBindingsManager;
    private ObjectProperty<RebindingInfo> rebindingInfoProperty = new SimpleObjectProperty<>();
    
    /**
     * Inner class containing information about an active key rebinding operation.
     * Stores the player mode, action, and button associated with a rebinding
     * operation in progress.
     */
    public static class RebindingInfo {
        /** The player mode (SINGLE, PLAYER1, or PLAYER2) for this rebinding */
        public final KeyBindingsManager.PlayerMode mode;
        /** The game action being rebound */
        public final KeyBindingsManager.Action action;
        /** The button that triggered the rebinding */
        public final Button button;
        
        /**
         * Creates a new RebindingInfo with the specified parameters.
         * 
         * @param mode The player mode for this rebinding
         * @param action The game action being rebound
         * @param button The button that triggered the rebinding
         */
        public RebindingInfo(KeyBindingsManager.PlayerMode mode, KeyBindingsManager.Action action, Button button) {
            this.mode = mode;
            this.action = action;
            this.button = button;
        }
    }

    /**
     * Creates a new SettingsPanel with all UI components initialized.
     * Sets up the layout with a "SETTINGS" title, volume control section (slider and mute button),
     * ghost piece checkbox, scrollable controls section for key rebinding, and a back button.
     * The panel is configured to be focusable and captures key events during rebinding operations.
     * All components are styled with CSS classes and the controls display is initially populated
     * with current key bindings for single player, player 1, and player 2.
     */
    public SettingsPanel() {
        getStyleClass().addAll("settings-panel", "animated");
        keyBindingsManager = KeyBindingsManager.getInstance();
        
        // Make panel focusable to receive key events
        setFocusTraversable(true);
        
        // Add key event handler to capture all keys during rebinding
        addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (isRebinding()) {
                handleRebindingKey(e.getCode());
                e.consume();
            }
        });

        VBox mainContainer = new VBox(20);
        mainContainer.getStyleClass().add("settings-container");
        mainContainer.setAlignment(Pos.CENTER);

        // SETTINGS title with enhanced styling - centered
        Label settingsLabel = new Label("SETTINGS");
        settingsLabel.getStyleClass().add("settings-title");

        // Volume control section with glass morphism effect
        VBox volumeBox = new VBox(20);
        volumeBox.getStyleClass().add("volume-section");
        volumeBox.setAlignment(Pos.TOP_LEFT);

        Label volumeLabel = new Label("VOLUME");
        volumeLabel.getStyleClass().add("settings-label");

        HBox volumeControlBox = new HBox(20);
        volumeControlBox.getStyleClass().add("volume-control-container");
        volumeControlBox.setAlignment(Pos.CENTER);

        volumeSlider = new Slider(0, 100, 50);
        volumeSlider.getStyleClass().add("volume-slider");
        volumeSlider.setShowTickLabels(false);
        volumeSlider.setShowTickMarks(false);
        volumeSlider.setPrefWidth(250);

        muteButton = new Button("🔊");
        muteButton.getStyleClass().add("settings-button");

        volumeControlBox.getChildren().addAll(volumeSlider, muteButton);
        volumeBox.getChildren().addAll(volumeLabel, volumeControlBox);

        // Ghost piece checkbox section
        VBox ghostPieceBox = new VBox(15);
        ghostPieceBox.setAlignment(Pos.TOP_LEFT);

        Label ghostPieceLabel = new Label("GHOST PIECE");
        ghostPieceLabel.getStyleClass().add("settings-label");
        ghostPieceLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        ghostPieceCheckBox = new CheckBox("Show ghost piece");
        ghostPieceCheckBox.getStyleClass().add("settings-checkbox");
        ghostPieceCheckBox.setSelected(true); // Default to checked

        ghostPieceBox.getChildren().addAll(ghostPieceLabel, ghostPieceCheckBox);

        // Controls customization section
        VBox controlsBox = new VBox(10);
        controlsBox.setAlignment(Pos.TOP_LEFT);
        controlsBox.setMaxHeight(280);

        Label controlsLabel = new Label("CONTROLS");
        controlsLabel.getStyleClass().add("settings-label");
        controlsLabel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        controlsContainer = new VBox(5);
        controlsContainer.setAlignment(Pos.CENTER);
        controlsContainer.setPadding(new Insets(5));
        controlsContainer.setMaxWidth(Double.MAX_VALUE);

        controlsScrollPane = new ScrollPane(controlsContainer);
        controlsScrollPane.getStyleClass().add("controls-scroll-pane");
        controlsScrollPane.setFitToWidth(true);
        controlsScrollPane.setPrefHeight(220);
        controlsScrollPane.setPrefWidth(590);
        controlsScrollPane.setMaxWidth(590);
        controlsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        controlsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        // Prevent arrow keys from scrolling when rebinding
        controlsScrollPane.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (isRebinding()) {
                e.consume();
            }
        });

        updateControlsDisplay();

        controlsBox.getChildren().addAll(controlsLabel, controlsScrollPane);

        // Back button with consistent main menu styling
        backButton = new Button("BACK");
        backButton.getStyleClass().add("menu-button");
        backButton.setOnAction(e -> {
            if (onBackAction != null) {
                onBackAction.run();
            }
        });

        mainContainer.getChildren().addAll(settingsLabel, volumeBox, ghostPieceBox, controlsBox, backButton);
        setCenter(mainContainer);
    }

    /**
     * Gets the volume slider control.
     * 
     * @return The Slider for adjusting volume (0-100)
     */
    public Slider getVolumeSlider() {
        return volumeSlider;
    }

    /**
     * Gets the mute button control.
     * 
     * @return The Button for toggling mute state
     */
    public Button getMuteButton() {
        return muteButton;
    }

    /**
     * Gets the ghost piece checkbox control.
     * 
     * @return The CheckBox for toggling ghost piece visibility
     */
    public CheckBox getGhostPieceCheckBox() {
        return ghostPieceCheckBox;
    }

    /**
     * Gets the back button control.
     * 
     * @return The Button for returning to the previous screen
     */
    public Button getBackButton() {
        return backButton;
    }

    /**
     * Sets the action to execute when the BACK button is clicked.
     * 
     * @param action The Runnable to execute when BACK is clicked, or null to remove the action
     */
    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }
    
    /**
     * Sets the audio manager for button sounds.
     * Configures click and hover sounds for all buttons in the panel, including
     * the back button and all rebind buttons.
     * 
     * @param audioManager The AudioManager instance to use for playing button sounds
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        setupButtonSounds();
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
     * Configures click and hover sounds for the back button and all rebind buttons.
     * Does nothing if audioManager is null.
     */
    private void setupButtonSounds() {
        if (audioManager == null) return;
        
        // Set up back button
        if (backButton != null) {
            setupButtonWithSound(backButton, audioManager);
        }
        
        // Set up all rebind buttons
        for (Button rebindButton : rebindButtons.values()) {
            if (rebindButton != null) {
                setupButtonWithSound(rebindButton, audioManager);
            }
        }
    }
    
    /**
     * Sets up sound effects for a single button.
     * Wraps the button's existing action handler to play a click sound before
     * executing the original action, and adds a hover sound on mouse enter.
     * Preserves the original action handler if one exists.
     * 
     * @param button The button to configure with sound effects
     * @param audioManager The AudioManager instance to use for playing sounds
     */
    private void setupButtonWithSound(Button button, AudioManager audioManager) {
        if (button == null || audioManager == null) return;
        
        javafx.event.EventHandler<javafx.event.ActionEvent> originalHandler = button.getOnAction();
        
        button.setOnAction(e -> {
            audioManager.playClickButton();
            if (originalHandler != null) {
                originalHandler.handle(e);
            }
        });
        
        button.setOnMouseEntered(e -> {
            audioManager.playHover();
        });
    }

    /**
     * Updates the controls display to show current key bindings.
     * Clears the existing controls container and rebuilds it with current
     * key bindings for single player, player 1, and player 2. Each player
     * mode displays its own section with action labels and rebind buttons.
     */
    public void updateControlsDisplay() {
        controlsContainer.getChildren().clear();
        rebindButtons.clear();

        // Single Player Controls
        VBox singlePlayerBox = createPlayerControlsBox("SINGLE PLAYER", KeyBindingsManager.PlayerMode.SINGLE);
        controlsContainer.getChildren().add(singlePlayerBox);

        // Player 1 Controls
        VBox player1Box = createPlayerControlsBox("PLAYER 1", KeyBindingsManager.PlayerMode.PLAYER1);
        controlsContainer.getChildren().add(player1Box);

        // Player 2 Controls
        VBox player2Box = createPlayerControlsBox("PLAYER 2", KeyBindingsManager.PlayerMode.PLAYER2);
        controlsContainer.getChildren().add(player2Box);
    }

    /**
     * Creates a VBox containing controls for a specific player mode.
     * Creates a labeled section with a grid of action labels and rebind buttons
     * for all applicable actions in the specified mode. Player 1 and Player 2
     * modes exclude PAUSE and NEW_GAME actions.
     * 
     * @param title The title label for this player mode section
     * @param mode The PlayerMode (SINGLE, PLAYER1, or PLAYER2) to create controls for
     * @return A VBox containing the player controls section
     */
    private VBox createPlayerControlsBox(String title, KeyBindingsManager.PlayerMode mode) {
        VBox playerBox = new VBox(5);
        playerBox.setAlignment(Pos.CENTER);
        playerBox.setPadding(new Insets(5, 0, 5, 0));
        playerBox.setMaxWidth(Double.MAX_VALUE);

        Label playerLabel = new Label(title);
        playerLabel.getStyleClass().add("controls-player-label");

        GridPane controlsGrid = new GridPane();
        controlsGrid.setHgap(15);
        controlsGrid.setVgap(5);
        controlsGrid.setAlignment(Pos.CENTER);
        controlsGrid.setPadding(new Insets(5, 10, 5, 10));
        
        // Set column constraints for better layout
        javafx.scene.layout.ColumnConstraints col1 = new javafx.scene.layout.ColumnConstraints();
        col1.setMinWidth(80);
        col1.setPrefWidth(100);
        col1.setMaxWidth(120);
        col1.setHgrow(javafx.scene.layout.Priority.NEVER);
        
        javafx.scene.layout.ColumnConstraints col2 = new javafx.scene.layout.ColumnConstraints();
        col2.setMinWidth(100);
        col2.setPrefWidth(120);
        col2.setMaxWidth(140);
        col2.setHgrow(javafx.scene.layout.Priority.NEVER);
        
        controlsGrid.getColumnConstraints().addAll(col1, col2);
        controlsGrid.setMaxWidth(250);

        int row = 0;
        for (KeyBindingsManager.Action action : KeyBindingsManager.Action.values()) {
            // Skip actions that don't apply to all modes
            if (mode == KeyBindingsManager.PlayerMode.PLAYER1 || mode == KeyBindingsManager.PlayerMode.PLAYER2) {
                if (action == KeyBindingsManager.Action.PAUSE || action == KeyBindingsManager.Action.NEW_GAME) {
                    continue;
                }
            }

            Label actionLabel = new Label(action.getName().replace("_", " "));
            actionLabel.getStyleClass().add("controls-action-label");
            actionLabel.setWrapText(true);
            actionLabel.setMaxWidth(Double.MAX_VALUE);

            String key = mode.getPrefix() + "." + action.getName();
            Button rebindButton = new Button(keyBindingsManager.getKeyBindingDisplay(mode, action));
            rebindButton.getStyleClass().add("rebind-button");
            rebindButton.setPrefWidth(120);
            rebindButton.setMinWidth(100);
            rebindButton.setMaxWidth(140);
            rebindButton.setWrapText(true);

            rebindButton.setOnAction(e -> startRebinding(key, mode, action, rebindButton));
            
            // Set up button sound if audio manager is available
            if (audioManager != null) {
                setupButtonWithSound(rebindButton, audioManager);
            }

            rebindButtons.put(key, rebindButton);

            controlsGrid.add(actionLabel, 0, row);
            controlsGrid.add(rebindButton, 1, row);
            row++;
        }

        playerBox.getChildren().addAll(playerLabel, controlsGrid);
        return playerBox;
    }

    /**
     * Starts a key rebinding operation for the specified action.
     * Cancels any existing rebinding operation, sets the button to "Press key" state,
     * adds active styling, and sets the rebinding info property to notify listeners.
     * 
     * @param key The key string identifier for this binding (mode.action format)
     * @param mode The PlayerMode for this rebinding
     * @param action The Action being rebound
     * @param button The Button that triggered the rebinding
     */
    private void startRebinding(String key, KeyBindingsManager.PlayerMode mode, KeyBindingsManager.Action action, Button button) {
        if (currentRebindingButton != null) {
            // Cancel previous rebinding
            String prevKey = rebindButtons.entrySet().stream()
                    .filter(entry -> entry.getValue() == currentRebindingButton)
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (prevKey != null) {
                KeyBindingsManager.PlayerMode prevMode = getModeFromKey(prevKey);
                KeyBindingsManager.Action prevAction = getActionFromKey(prevKey);
                if (prevMode != null && prevAction != null) {
                    currentRebindingButton.setText(keyBindingsManager.getKeyBindingDisplay(prevMode, prevAction));
                }
            }
            currentRebindingButton.getStyleClass().remove("rebind-button-active");
        }

        currentRebindingButton = button;
        button.setText("Press key");
        button.getStyleClass().add("rebind-button-active");
        
        // Set rebinding info property so GuiController can handle it
        rebindingInfoProperty.set(new RebindingInfo(mode, action, button));
    }
    
    /**
     * Handles a key press during rebinding operation.
     * Checks for conflicts with other players (for PLAYER1 and PLAYER2 modes),
     * handles key swapping if the key is already bound to another action in the
     * same mode, sets the new binding, saves to file, and updates the UI.
     * Shows a warning dialog if a conflict is detected with another player.
     * 
     * @param newKey The KeyCode of the key that was pressed
     */
    public void handleRebindingKey(KeyCode newKey) {
        RebindingInfo info = rebindingInfoProperty.get();
        if (info == null) {
            return;
        }
        
        KeyBindingsManager.PlayerMode mode = info.mode;
        KeyBindingsManager.Action action = info.action;
        Button button = info.button;
        
        // Check for conflicts with other player (only for PLAYER1 and PLAYER2)
        if (mode == KeyBindingsManager.PlayerMode.PLAYER1 || mode == KeyBindingsManager.PlayerMode.PLAYER2) {
            KeyBindingsManager.PlayerMode conflictingMode = keyBindingsManager.getConflictingPlayerMode(newKey, mode);
            if (conflictingMode != null) {
                // Conflict detected - show warning and prevent binding
                KeyBindingsManager.Action conflictingAction = keyBindingsManager.getActionInPlayerMode(newKey, conflictingMode);
                String conflictingPlayerName = (conflictingMode == KeyBindingsManager.PlayerMode.PLAYER1) ? "Player 1" : "Player 2";
                String actionName = (conflictingAction != null) ? conflictingAction.getName().replace("_", " ") : "an action";
                
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Key Conflict");
                alert.setHeaderText("Key Already Assigned");
                alert.setContentText("This key is already assigned to " + conflictingPlayerName + " for " + actionName + ".\n\nPlease choose a different key.");
                alert.showAndWait();
                
                // Restore button text and exit rebinding mode
                button.setText(keyBindingsManager.getKeyBindingDisplay(mode, action));
                button.getStyleClass().remove("rebind-button-active");
                currentRebindingButton = null;
                rebindingInfoProperty.set(null);
                return;
            }
        }
        
        // Check if key is already bound to another action in the same mode
        KeyBindingsManager.Action existingAction = keyBindingsManager.getActionForKey(newKey, mode);
        if (existingAction != null && existingAction != action) {
            // Swap the keys
            KeyCode oldKey = keyBindingsManager.getKeyBinding(mode, action);
            keyBindingsManager.setKeyBinding(mode, existingAction, oldKey);
            updateButtonForAction(mode, existingAction);
        }

        // Set the new binding
        keyBindingsManager.setKeyBinding(mode, action, newKey);
        keyBindingsManager.saveKeyBindings();
        
        button.setText(keyBindingsManager.getKeyBindingDisplay(mode, action));
        button.getStyleClass().remove("rebind-button-active");
        currentRebindingButton = null;
        rebindingInfoProperty.set(null);
    }
    
    /**
     * Gets the rebinding info property for external listeners.
     * This property is set when rebinding starts and cleared when rebinding completes.
     * External components can listen to this property to handle rebinding operations.
     * 
     * @return The ObjectProperty containing the current RebindingInfo, or null if not rebinding
     */
    public ObjectProperty<RebindingInfo> rebindingInfoProperty() {
        return rebindingInfoProperty;
    }
    
    /**
     * Checks if a key rebinding operation is currently in progress.
     * 
     * @return true if rebinding is active, false otherwise
     */
    public boolean isRebinding() {
        return rebindingInfoProperty.get() != null;
    }

    /**
     * Updates the button text for a specific action after a key swap.
     * Used when swapping keys between actions in the same mode.
     * 
     * @param mode The PlayerMode for the action
     * @param action The Action whose button should be updated
     */
    private void updateButtonForAction(KeyBindingsManager.PlayerMode mode, KeyBindingsManager.Action action) {
        String key = mode.getPrefix() + "." + action.getName();
        Button button = rebindButtons.get(key);
        if (button != null) {
            button.setText(keyBindingsManager.getKeyBindingDisplay(mode, action));
        }
    }

    /**
     * Extracts the PlayerMode from a key string identifier.
     * 
     * @param key The key string in format "mode.action"
     * @return The PlayerMode extracted from the key, or null if invalid
     */
    private KeyBindingsManager.PlayerMode getModeFromKey(String key) {
        if (key.startsWith("single.")) return KeyBindingsManager.PlayerMode.SINGLE;
        if (key.startsWith("player1.")) return KeyBindingsManager.PlayerMode.PLAYER1;
        if (key.startsWith("player2.")) return KeyBindingsManager.PlayerMode.PLAYER2;
        return null;
    }

    /**
     * Extracts the Action from a key string identifier.
     * 
     * @param key The key string in format "mode.action"
     * @return The Action extracted from the key, or null if invalid
     */
    private KeyBindingsManager.Action getActionFromKey(String key) {
        String actionName = key.substring(key.indexOf('.') + 1);
        for (KeyBindingsManager.Action action : KeyBindingsManager.Action.values()) {
            if (action.getName().equals(actionName)) {
                return action;
            }
        }
        return null;
    }

}