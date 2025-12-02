package com.comp2042.controller.manager;

import com.comp2042.controller.SettingsController;
import com.comp2042.view.SinglePlayerScreen;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.util.List;

/**
 * Manages the countdown sequence before starting a game session.
 * This class coordinates the visual and audio countdown (3-2-1) that occurs before gameplay begins.
 * It handles UI visibility management (hiding next bricks, showing game panels), plays countdown
 * sound effects, displays countdown numbers with proper centering, coordinates with the game loop
 * manager for timeline animations, and executes a completion callback when the countdown finishes.
 * The countdown ensures players are ready and provides a smooth transition from the ready state
 * to active gameplay.
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
    
    /**
     * Creates a new CountdownManager with the specified dependencies.
     * 
     * @param audioManager The audio manager for playing countdown sounds
     * @param panelCoordinator The panel coordinator for managing panel visibility
     * @param gameLoopManager The game loop manager for timeline operations
     */
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
     * 
     * @param countdownLabel The label to display the countdown numbers
     */
    public void setCountdownLabel(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }
    
    /**
     * Sets the game stack pane (for centering the label).
     * 
     * @param gameStack The StackPane container for the countdown label
     */
    public void setGameStack(StackPane gameStack) {
        this.gameStack = gameStack;
    }
    
    /**
     * Sets the ghost panel (for visibility control).
     * 
     * @param ghostPanel The GridPane for the ghost piece display
     */
    public void setGhostPanel(GridPane ghostPanel) {
        this.ghostPanel = ghostPanel;
    }
    
    /**
     * Sets the single player screen (for accessing next brick panes).
     * 
     * @param singlePlayerScreen The SinglePlayerScreen instance
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Sets the settings controller (for checking ghost piece setting).
     * 
     * @param settingsController The SettingsController instance
     */
    public void setSettingsController(SettingsController settingsController) {
        this.settingsController = settingsController;
    }
    
    /**
     * Sets the callback to be called when countdown completes.
     * 
     * @param onCountdownComplete The Runnable callback to execute when countdown finishes
     */
    public void setOnCountdownComplete(Runnable onCountdownComplete) {
        this.onCountdownComplete = onCountdownComplete;
    }
    
    /**
     * Starts the countdown sequence before game begins.
     * Performs the following operations:
     * <ul>
     *   <li>Stops main menu music</li>
     *   <li>Shows game panel, brick panel, and ghost panel (if enabled)</li>
     *   <li>Hides next brick preview panes during countdown</li>
     *   <li>Plays countdown sound effect</li>
     *   <li>Displays and centers the countdown label</li>
     *   <li>Shows initial countdown number (3)</li>
     *   <li>Sets up countdown callbacks for tick updates and completion</li>
     *   <li>Creates and starts the countdown timeline animation</li>
     * </ul>
     * If the countdown label is not set, the completion callback is executed immediately
     * without performing the countdown sequence.
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

