package com.comp2042.controller.manager;

import com.comp2042.controller.GameConstants;
import com.comp2042.event.InputEventListener;
import com.comp2042.bricks.Brick;
import com.comp2042.model.ViewData;
import com.comp2042.view.GameViewRenderer;
import com.comp2042.view.SinglePlayerScreen;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * Manages single player view operations and UI state transitions.
 * This class coordinates the display and rendering of all visual elements in single player mode,
 * including the current brick, ghost piece, game board background, next brick previews, and hold
 * brick. It handles visibility logic based on game state (e.g., hiding bricks during countdown),
 * delegates actual rendering to GameViewRenderer, and manages the relationship between the game
 * logic and the single player screen UI. The manager ensures that visual updates only occur when
 * appropriate (e.g., after game start) and coordinates with PanelCoordinator for panel visibility.
 */
public class SinglePlayerViewManager {
    
    // Dependencies
    private final GameStateManager gameStateManager;
    private final PanelCoordinator panelCoordinator;
    
    // UI Components (set via setters)
    private SinglePlayerScreen singlePlayerScreen;
    private final GameViewRenderer renderer = new GameViewRenderer();
    
    // Constants
    private static final int BRICK_SIZE = GameConstants.BRICK_SIZE;
    
    /**
     * Creates a new SinglePlayerViewManager with the specified dependencies.
     * 
     * @param gameStateManager The game state manager for checking game state
     * @param panelCoordinator The panel coordinator for managing panel visibility
     */
    public SinglePlayerViewManager(
            GameStateManager gameStateManager,
            PanelCoordinator panelCoordinator) {
        this.gameStateManager = gameStateManager;
        this.panelCoordinator = panelCoordinator;
    }
    
    // Setters for UI components
    /**
     * Sets the single player screen reference.
     * 
     * @param singlePlayerScreen The SinglePlayerScreen instance for single player mode
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Refreshes the brick display with updated brick data.
     * Always stores the current brick data, but only renders the brick visually
     * if the game has started (to prevent showing bricks during main menu or countdown).
     * Delegates the actual rendering to GameViewRenderer, which handles both the
     * current brick and ghost piece display.
     * 
     * @param brick The ViewData containing brick position, shape, and state information
     */
    public void refreshBrick(ViewData brick) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        // Always store the current brick data
        if (brick != null) {
            singlePlayerScreen.setCurrentBrickData(brick);
        }
        
        // Don't show brick if game hasn't started (menu is visible)
        if (!gameStateManager.isGameStarted()) {
            return;
        }
        
        // Get panels from screen
        GridPane brickPanel = singlePlayerScreen.getBrickPanel();
        GridPane ghostPanel = singlePlayerScreen.getGhostPanel();
        InputEventListener eventListener = singlePlayerScreen.getEventListener();
        
        // Delegate rendering to GameViewRenderer
        renderer.refreshBrick(brick, brickPanel, ghostPanel, eventListener, BRICK_SIZE);
    }
    
    /**
     * Refreshes the game background display with the current board state.
     * Updates the visual representation of all placed blocks on the game board.
     * Delegates the actual rendering to GameViewRenderer.
     * 
     * @param board The 2D integer array representing the game board matrix
     */
    public void refreshGameBackground(int[][] board) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        // Delegate rendering to GameViewRenderer
        renderer.refreshGameBackground(board, singlePlayerScreen.getDisplayMatrix());
    }
    
    /**
     * Updates the next bricks display with the upcoming brick previews.
     * Handles visibility logic by showing next brick panes only when the game
     * has started (after countdown), hiding them during main menu or countdown.
     * Calculates appropriate brick size for previews and delegates rendering to
     * GameViewRenderer, which displays up to the maximum number of available panes.
     * 
     * @param nextBricks The list of Brick objects representing upcoming pieces
     */
    public void updateNextBricks(List<Brick> nextBricks) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        List<GridPane> nextBrickPanes = singlePlayerScreen.getNextBrickPanes();
        if (nextBrickPanes == null || nextBrickPanes.isEmpty()) {
            return;
        }
        
        int brickSize = BRICK_SIZE - 10;
        int maxBricks = nextBrickPanes.size();
        
        // Only make panes visible when game has started (after countdown)
        // Don't show bricks during main menu or countdown
        if (gameStateManager.isGameStarted()) {
            panelCoordinator.showNextBrickPanes(nextBrickPanes);
        } else {
            panelCoordinator.hideNextBrickPanes(nextBrickPanes);
        }
        
        // Delegate rendering to GameViewRenderer
        renderer.updateNextBricks(nextBricks, nextBrickPanes, brickSize, maxBricks);
    }
    
    /**
     * Updates the hold brick display with the currently held brick.
     * If no brick is held, clears the hold brick display. Delegates the actual
     * rendering to GameViewRenderer.
     * 
     * @param heldBrick The Brick object being held, or null if no brick is held
     */
    public void updateHoldBrick(Brick heldBrick) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        // Delegate rendering to GameViewRenderer
        renderer.renderHoldBrick(heldBrick, singlePlayerScreen.getHoldBrickRectangles());
    }
    
    /**
     * Gets the current brick data stored in the single player screen.
     * 
     * @return The ViewData containing current brick information, or null if no brick data is stored
     */
    public ViewData getCurrentBrickData() {
        if (singlePlayerScreen == null) {
            return null;
        }
        return singlePlayerScreen.getCurrentBrickData();
    }
    
    /**
     * Sets the current brick data on the single player screen.
     * Stores the brick data for later retrieval or rendering.
     * 
     * @param brickData The ViewData containing brick information to store
     */
    public void setCurrentBrickData(ViewData brickData) {
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setCurrentBrickData(brickData);
        }
    }
    
    /**
     * Sets the event listener for the single player screen.
     * The event listener is used for processing game move events and determining
     * ghost piece positions.
     * 
     * @param eventListener The InputEventListener instance for processing game events
     */
    public void setEventListener(InputEventListener eventListener) {
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setEventListener(eventListener);
        }
    }
}

