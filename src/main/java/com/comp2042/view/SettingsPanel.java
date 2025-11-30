package com.comp2042.view;

import com.comp2042.controller.manager.AudioManager;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
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
    
    public static class RebindingInfo {
        public final KeyBindingsManager.PlayerMode mode;
        public final KeyBindingsManager.Action action;
        public final Button button;
        
        public RebindingInfo(KeyBindingsManager.PlayerMode mode, KeyBindingsManager.Action action, Button button) {
            this.mode = mode;
            this.action = action;
            this.button = button;
        }
    }

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

    public Slider getVolumeSlider() {
        return volumeSlider;
    }

    public Button getMuteButton() {
        return muteButton;
    }

    public CheckBox getGhostPieceCheckBox() {
        return ghostPieceCheckBox;
    }

    public Button getBackButton() {
        return backButton;
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }
    
    /**
     * Sets the audio manager for button sounds.
     * @param audioManager The audio manager to use
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
        setupButtonSounds();
    }
    
    /**
     * Sets up button sounds for all buttons in this panel.
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
    
    public void handleRebindingKey(KeyCode newKey) {
        RebindingInfo info = rebindingInfoProperty.get();
        if (info == null) {
            return;
        }
        
        KeyBindingsManager.PlayerMode mode = info.mode;
        KeyBindingsManager.Action action = info.action;
        Button button = info.button;
        
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
    
    public ObjectProperty<RebindingInfo> rebindingInfoProperty() {
        return rebindingInfoProperty;
    }
    
    public boolean isRebinding() {
        return rebindingInfoProperty.get() != null;
    }

    private void updateButtonForAction(KeyBindingsManager.PlayerMode mode, KeyBindingsManager.Action action) {
        String key = mode.getPrefix() + "." + action.getName();
        Button button = rebindButtons.get(key);
        if (button != null) {
            button.setText(keyBindingsManager.getKeyBindingDisplay(mode, action));
        }
    }

    private KeyBindingsManager.PlayerMode getModeFromKey(String key) {
        if (key.startsWith("single.")) return KeyBindingsManager.PlayerMode.SINGLE;
        if (key.startsWith("player1.")) return KeyBindingsManager.PlayerMode.PLAYER1;
        if (key.startsWith("player2.")) return KeyBindingsManager.PlayerMode.PLAYER2;
        return null;
    }

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