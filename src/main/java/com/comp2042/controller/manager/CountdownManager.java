package com.comp2042.controller.manager;

import com.comp2042.controller.SettingsController;
import com.comp2042.view.SinglePlayerScreen;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Manages the countdown sequence before starting a game.
 * Handles hiding next bricks, playing sound, showing countdown numbers,
 * centering the label, animations, and starting the timeline.
 */
public class CountdownManager {
    
    // UI Components
    private Label countdownLabel;
    private StackPane gameStack;
    private GridPane ghostPanel;
    
    // Dependencies
    private final AudioManager audioManager;
    private final PanelCoordinator panelCoordinator;
    private final GameLoopManager gameLoopManager;
    private SinglePlayerScreen singlePlayerScreen;
    private SettingsController settingsController;
    
    // Callback for when countdown completes
    private Runnable onCountdownComplete;
    
    public CountdownManager(
            AudioManager audioManager,
            PanelCoordinator panelCoordinator,
            GameLoopManager gameLoopManager) {
        this.audioManager = audioManager;
        this.panelCoordinator = panelCoordinator;
        this.gameLoopManager = gameLoopManager;
    }
    
    /**
     * Sets the countdown label.
     */
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    /**
     * Sets the game stack pane (for centering the label).
     */
    public void setGameStack(StackPane gameStack) {
        this.gameStack = gameStack;
    }
    
    /**
     * Sets the ghost panel (for visibility control).
     */
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    /**
     * Sets the single player screen (for accessing next brick panes).
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Sets the settings controller (for checking ghost piece setting).
     */
    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }
    
    /**
     * Sets the callback to be called when countdown completes.
     */
    public void setOnCountdownComplete(Runnable onCountdownComplete) {
        this.onCountdownComplete = onCountdownComplete;
    }
    
    /**
     * Starts the countdown sequence.
     * Hides next bricks, plays sound, shows countdown numbers,
     * centers the label, and starts the timeline.
     */
    public void startCountdown() {
        if (countdownLabel == null) {
            // If countdown label doesn't exist, call completion callback immediately
            if (onCountdownComplete != null) {
                onCountdownComplete.run();
            }
            return;
        }
        
        // Stop main menu music during countdown
        audioManager.stopMainMenuMusic();
        
        // Make game panel visible during countdown so grid is visible
        panelCoordinator.showGamePanel();
        // Make brick panel and ghost panel visible during countdown
        panelCoordinator.showBrickPanel();
        if (ghostPanel != null && settingsController != null) {
            // Check if ghost piece checkbox is selected
            boolean showGhost = settingsController.isGhostPieceEnabled();
            panelCoordinator.showGhostPanel(showGhost);
        }
        
        // Hide next bricks during countdown - they'll be shown after countdown completes
        if (singlePlayerScreen != null) {
            List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
            if (nextBrickPanes != null) {
                panelCoordinator.hideNextBrickPanes(nextBrickPanes);
            }
        }
        
        // Play countdown sound
        audioManager.playCountdown();
        
        // Make countdown label visible and center it
        panelCoordinator.showCountdownLabel();
        countdownLabel.setAlignment(javafx.geometry.Pos.CENTER);
        
        // Center the label in the StackPane
        if (gameStack != null) {
            StackPane.setAlignment(countdownLabel, javafx.geometry.Pos.CENTER);
        }
        
        // Ensure label fills the StackPane for proper centering
        countdownLabel.setMaxWidth(Double.MAX_VALUE);
        countdownLabel.setMaxHeight(Double.MAX_VALUE);
        
        // Show initial countdown number (3)
        countdownLabel.setText("3");
        
        // Set up countdown callbacks
        gameLoopManager.setCountdownCallbacks(new GameLoopManager.CountdownCallbacks() {
            @Override
            public void onCountdownComplete() {
                panelCoordinator.hideCountdownLabel();
                if (onCountdownComplete != null) {
                    onCountdownComplete.run();
                }
            }
            
            @Override
            public void onCountdownTick(int count) {
                if (countdownLabel != null && count > 0) {
                    countdownLabel.setText(String.valueOf(count));
                }
            }
        });
        
        // Create and start countdown timeline
        gameLoopManager.createCountdownTimeline();
    }
}

