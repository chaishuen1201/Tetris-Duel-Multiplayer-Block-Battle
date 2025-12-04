package com.comp2042.controller;

import com.comp2042.controller.manager.AudioManager;
import com.comp2042.controller.manager.GameStateManager;
import com.comp2042.controller.manager.PanelCoordinator;
import com.comp2042.controller.manager.MultiplayerViewManager;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SettingsPanel;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/**
 * Manages the settings panel functionality including volume control, mute toggle,
 * ghost piece visibility, and key rebinding operations. This class coordinates
 * the settings panel UI, handling its display in both single player and multiplayer
 * modes. It manages volume slider bindings, mute button state, ghost piece checkbox
 * state, and ensures the settings panel is displayed in the correct container
 * (gameStack for single player, overlay for multiplayer). The controller coordinates
 * with PanelCoordinator for panel visibility, AudioManager for audio settings,
 * GameStateManager for game state checks, and MultiplayerViewManager for multiplayer
 * view updates. It follows the Single Responsibility Principle by exclusively
 * handling settings panel operations.
 */
public class SettingsController {
    
    private SettingsPanel settingsPanel;
    private AudioManager audioManager;
    private PanelCoordinator panelCoordinator;
    private GameStateManager gameStateManager;
    private MultiplayerScreen multiplayerScreen;
    private MultiplayerViewManager multiplayerViewManager;
    private StackPane gameStack;
    private MainMenuPanel mainMenuPanel;
    private javafx.scene.layout.GridPane ghostPanel;
    
    /**
     * Creates a new SettingsController instance.
     * The controller must be initialized using the initialize() method before use
     * to set up all required dependencies and configure the settings panel.
     */
    public SettingsController() {
    }
    
    /**
     * Initializes the settings controller with all required dependencies.
     * Sets up volume slider, mute button, ghost piece checkbox, and back button actions.
     * 
     * @param settingsPanel The settings panel UI component
     * @param audioManager The audio manager for volume and mute control
     * @param panelCoordinator The panel coordinator for managing panel visibility
     * @param gameStateManager The game state manager for checking game state
     * @param multiplayerScreen The multiplayer screen for multiplayer-specific operations
     * @param gameStack The stack pane container for the settings panel
     * @param mainMenuPanel The main menu panel reference
     * @param ghostPanel The ghost panel for ghost piece visibility control
     */
    public void initialize(
            SettingsPanel settingsPanel,
            AudioManager audioManager,
            PanelCoordinator panelCoordinator,
            GameStateManager gameStateManager,
            MultiplayerScreen multiplayerScreen,
            StackPane gameStack,
            MainMenuPanel mainMenuPanel,
            javafx.scene.layout.GridPane ghostPanel) {
        
        this.settingsPanel = settingsPanel;
        this.audioManager = audioManager;
        this.panelCoordinator = panelCoordinator;
        this.gameStateManager = gameStateManager;
        this.multiplayerScreen = multiplayerScreen;
        this.gameStack = gameStack;
        this.mainMenuPanel = mainMenuPanel;
        this.ghostPanel = ghostPanel;
        
        if (settingsPanel != null) {
            panelCoordinator.hideSettingsPanel();
            
            // Set up volume slider
            javafx.scene.control.Slider volumeSlider = settingsPanel.getVolumeSlider();
            volumeSlider.setValue(audioManager.getVolume() * 100); // Convert to percentage
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double newVolume = newVal.doubleValue() / 100.0;
                audioManager.setVolume(newVolume);
                // If user moves slider while muted, unmute
                if (audioManager.isMuted() && newVal.doubleValue() != oldVal.doubleValue()) {
                    audioManager.toggleMute();
                    Button muteBtn = settingsPanel.getMuteButton();
                    if (muteBtn != null) {
                        muteBtn.setText("🔊");
                    }
                }
            });
            
            // Set up mute button
            Button muteButton = settingsPanel.getMuteButton();
            // Initialize mute button text based on current state
            muteButton.setText(audioManager.isMuted() ? "🔇" : "🔊");
            muteButton.setOnAction(e -> {
                audioManager.playClickButton();
                boolean isMuted = audioManager.toggleMute();
                muteButton.setText(isMuted ? "🔇" : "🔊");
            });
            muteButton.setOnMouseEntered(e -> {
                audioManager.playHover();
            });
            
            // Set up button sounds for all buttons in settings panel
            settingsPanel.setAudioManager(audioManager);
            
            // Set up ghost piece checkbox
            javafx.scene.control.CheckBox ghostPieceCheckBox = settingsPanel.getGhostPieceCheckBox();
            ghostPieceCheckBox.setSelected(true); // Default to checked
            ghostPieceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Update ghost panel for single player
                panelCoordinator.showGhostPanel(newVal && gameStateManager.isGameStarted());
                // Update ghost panels for multiplayer
                // Brick panels should always be visible when game is started, only ghost panel visibility changes
                if (gameStateManager.isMultiplayerMode() && multiplayerViewManager != null && gameStateManager.isGameStarted()) {
                    multiplayerViewManager.setBrickPanelsVisible(true, settingsPanel);
                }
            });
            
            // Set up back button
            settingsPanel.setOnBackAction(() -> hideSettings());
        }
    }
    
    /**
     * Shows the settings panel from the main menu.
     * Hides the main menu panel, ensures the settings panel is in the correct container
     * (gameStack for single player mode), removes it from multiplayer overlay if present,
     * updates the controls display to show current key bindings, requests focus for
     * key rebinding operations, and forces a layout update to ensure proper display.
     */
    public void showSettings() {
        if (settingsPanel != null && mainMenuPanel != null) {
            panelCoordinator.hideMainMenuPanel();
            
            // Ensure settings panel is in gameStack for single player mode (from main menu)
            if (gameStack != null && settingsPanel != null) {
                // Remove from any other parent first (defensive check)
                javafx.scene.Parent currentParent = settingsPanel.getParent();
                if (currentParent != null && currentParent != gameStack) {
                    if (currentParent instanceof javafx.scene.layout.Pane) {
                        ((javafx.scene.layout.Pane) currentParent).getChildren().remove(settingsPanel);
                    }
                }
                // Remove from multiplayer overlay if it's there (defensive check)
                if (multiplayerScreen != null) {
                    multiplayerScreen.hideSettingsOverlay();
                }
                // Add back to gameStack if not already there (defensive check)
                if (!gameStack.getChildren().contains(settingsPanel)) {
                    gameStack.getChildren().add(settingsPanel);
                }
                // Bring settings panel to front in gameStack (so it's on top of other elements)
                gameStack.getChildren().remove(settingsPanel);
                gameStack.getChildren().add(settingsPanel);
            }
            
            // Show settings panel
            panelCoordinator.showSettingsPanelManaged();
            
            // Refresh controls display to show current bindings
            settingsPanel.updateControlsDisplay();
            
            // Request focus on settings panel to receive key events
            settingsPanel.requestFocus();
            
            // Force a layout update to ensure it's displayed
            if (gameStack != null) {
                gameStack.requestLayout();
            }
        }
    }
    
    /**
     * Shows the settings panel from the pause menu.
     * Handles both single player and multiplayer modes, ensuring the panel is displayed
     * in the correct container (gameStack for single player, overlay for multiplayer).
     * Updates the controls display, requests focus for key rebinding, hides the pause
     * panel, and ensures proper container placement based on the current game mode.
     */
    public void showSettingsFromPause() {
        if (settingsPanel != null) {
            // Refresh controls display to show current bindings
            settingsPanel.updateControlsDisplay();
            // Request focus on settings panel to receive key events
            settingsPanel.requestFocus();
            
            if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                // Hide pause overlay for multiplayer
                multiplayerScreen.hidePausePanel();
                // Remove settings panel from gameStack if it's there (needed for multiplayer)
                if (gameStack != null && settingsPanel != null) {
                    javafx.scene.Parent currentParent = settingsPanel.getParent();
                    if (currentParent != null && currentParent == gameStack) {
                        gameStack.getChildren().remove(settingsPanel);
                    }
                }
                // Show settings overlay
                multiplayerScreen.showSettingsOverlay(settingsPanel);
            } else {
                // Hide pause panel for single player
                panelCoordinator.hidePausePanel();
                // Ensure settings panel is in gameStack for single player
                if (gameStack != null && settingsPanel != null) {
                    // Remove from any other parent first (defensive check)
                    javafx.scene.Parent currentParent = settingsPanel.getParent();
                    if (currentParent != null && currentParent != gameStack) {
                        if (currentParent instanceof javafx.scene.layout.Pane) {
                            ((javafx.scene.layout.Pane) currentParent).getChildren().remove(settingsPanel);
                        }
                    }
                    // Remove from multiplayer overlay if it's there (defensive check)
                    if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                        multiplayerScreen.hideSettingsOverlay();
                    }
                    // Add back to gameStack if not already there (defensive check)
                    if (!gameStack.getChildren().contains(settingsPanel)) {
                        gameStack.getChildren().add(settingsPanel);
                    }
                    // Bring settings panel to front in gameStack (so it's on top of other elements)
                    gameStack.getChildren().remove(settingsPanel);
                    gameStack.getChildren().add(settingsPanel);
                }
                // Show settings panel (it's in gameStack for single player)
                if (settingsPanel != null) {
                    // Ensure it's managed and visible
                    panelCoordinator.showSettingsPanelManaged();
                    // Force a layout update to ensure it's displayed
                    if (gameStack != null) {
                        gameStack.requestLayout();
                    }
                }
            }
        }
    }
    
    /**
     * Hides the settings panel and returns to either the pause menu or main menu
     * depending on the current game state. Handles both single player and multiplayer modes.
     * For multiplayer, hides the settings overlay and shows pause panel if game is paused,
     * or shows main menu otherwise. For single player, hides the settings panel and shows
     * pause panel if game is paused, or shows main menu otherwise. The settings panel
     * remains in gameStack for single player mode for future use.
     */
    public void hideSettings() {
        if (settingsPanel != null) {
            if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                // Hide settings overlay for multiplayer
                multiplayerScreen.hideSettingsOverlay();
                panelCoordinator.hideSettingsPanel();
                
                // Check if we should return to pause menu or main menu
                if (gameStateManager.isPaused() && gameStateManager.isGameStarted()) {
                    // Return to pause menu for multiplayer
                    multiplayerScreen.showPausePanel();
                } else {
                    // Return to main menu (shouldn't happen in multiplayer, but handle it)
                    panelCoordinator.showMainMenuPanel();
                }
            } else {
                // Hide settings panel for single player
                panelCoordinator.hideSettingsPanel();
                // Note: Keep settingsPanel in gameStack for next time
                
                // Check if we should return to pause menu or main menu
                if (gameStateManager.isPaused() && gameStateManager.isGameStarted()) {
                    // Return to pause menu for single player
                    panelCoordinator.showPausePanel();
                } else {
                    // Return to main menu
                    panelCoordinator.showMainMenuPanel();
                }
            }
        }
    }
    
    /**
     * Checks if the ghost piece feature is enabled based on the checkbox state.
     * 
     * @return True if ghost piece is enabled, false otherwise. Defaults to true if settings are not initialized.
     */
    public boolean isGhostPieceEnabled() {
        if (settingsPanel != null && ghostPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            return ghostCheckBox != null && ghostCheckBox.isSelected();
        }
        return true; // Default to enabled
    }
    
    /**
     * Ensures the settings panel is in the correct container (gameStack) for single player mode.
     * Removes it from multiplayer overlay if present, adds it to gameStack if needed,
     * and hides the settings panel. This method is called defensively to ensure proper
     * container placement when transitioning from multiplayer to single player mode.
     */
    public void ensureSettingsPanelInGameStack() {
        if (!gameStateManager.isMultiplayerMode() && gameStack != null && settingsPanel != null) {
            // Remove from multiplayer overlay if it's there
            if (multiplayerScreen != null) {
                multiplayerScreen.hideSettingsOverlay();
            }
            // Ensure it's in gameStack
            if (!gameStack.getChildren().contains(settingsPanel)) {
                gameStack.getChildren().add(settingsPanel);
            }
            // Hide settings panel (it will be shown when needed)
            panelCoordinator.hideSettingsPanel();
        }
    }
    
    /**
     * Sets the multiplayer screen reference for multiplayer-specific operations.
     * 
     * @param multiplayerScreen The multiplayer screen instance
     */
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    /**
     * Sets the multiplayer view manager reference for coordinating multiplayer view updates.
     * 
     * @param multiplayerViewManager The multiplayer view manager instance
     */
    public void setMultiplayerViewManager(MultiplayerViewManager multiplayerViewManager) {
        this.multiplayerViewManager = multiplayerViewManager;
    }

}

