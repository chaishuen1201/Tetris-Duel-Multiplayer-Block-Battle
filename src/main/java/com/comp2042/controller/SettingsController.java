package com.comp2042.controller;

import com.comp2042.controller.manager.AudioManager;
import com.comp2042.controller.manager.GameStateManager;
import com.comp2042.controller.manager.PanelCoordinator;
import com.comp2042.view.MainMenuPanel;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SettingsPanel;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class SettingsController {
    
    private SettingsPanel settingsPanel;
    private AudioManager audioManager;
    private PanelCoordinator panelCoordinator;
    private GameStateManager gameStateManager;
    private MultiplayerScreen multiplayerScreen;
    private StackPane gameStack;
    private MainMenuPanel mainMenuPanel;
    private javafx.scene.layout.GridPane ghostPanel;
    
    public SettingsController() {
    }
    
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
                boolean isMuted = audioManager.toggleMute();
                muteButton.setText(isMuted ? "🔇" : "🔊");
            });
            
            // Set up ghost piece checkbox
            javafx.scene.control.CheckBox ghostPieceCheckBox = settingsPanel.getGhostPieceCheckBox();
            ghostPieceCheckBox.setSelected(true); // Default to checked
            ghostPieceCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                // Update ghost panel for single player
                panelCoordinator.showGhostPanel(newVal && gameStateManager.isGameStarted());
                // Update ghost panels for multiplayer
                if (gameStateManager.isMultiplayerMode() && multiplayerScreen != null) {
                    multiplayerScreen.setBrickPanelsVisible(newVal && gameStateManager.isGameStarted(), settingsPanel);
                }
            });
            
            // Set up back button
            settingsPanel.setOnBackAction(() -> hideSettings());
        }
    }
    
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
    
    // Method to check if ghost piece is enabled (used by other controllers)
    public boolean isGhostPieceEnabled() {
        if (settingsPanel != null && ghostPanel != null) {
            javafx.scene.control.CheckBox ghostCheckBox = settingsPanel.getGhostPieceCheckBox();
            return ghostCheckBox != null && ghostCheckBox.isSelected();
        }
        return true; // Default to enabled
    }
    
    // Method to ensure settings panel is in correct container (used by other controllers)
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
    
    // Setters for updating references (e.g., when multiplayer screen is created)
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    public SettingsPanel getSettingsPanel() {
        return settingsPanel;
    }
}

