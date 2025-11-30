package com.comp2042.controller.manager;

import com.comp2042.controller.GameConstants;
import com.comp2042.event.InputEventListener;
import com.comp2042.logic.bricks.Brick;
import com.comp2042.model.ViewData;
import com.comp2042.view.GameViewRenderer;
import com.comp2042.view.SinglePlayerScreen;
import javafx.scene.layout.GridPane;

import java.util.List;

/**
 * Manages single player view operations and UI state transitions.
 * Handles refreshing bricks, game background, next bricks, hold brick, and visibility logic.
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
    
    public SinglePlayerViewManager(
            GameStateManager gameStateManager,
            PanelCoordinator panelCoordinator) {
        this.gameStateManager = gameStateManager;
        this.panelCoordinator = panelCoordinator;
    }
    
    // Setters for UI components
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Refreshes the brick display.
     * Handles gameStarted condition - doesn't show brick if game hasn't started.
     * 
     * @param brick The brick data to display
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
     * Refreshes the game background display.
     * 
     * @param board The board matrix to display
     */
    public void refreshGameBackground(int[][] board) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        // Delegate rendering to GameViewRenderer
        renderer.refreshGameBackground(board, singlePlayerScreen.getDisplayMatrix());
    }
    
    /**
     * Updates the next bricks display.
     * Handles visibility logic - only shows next bricks when game has started.
     * 
     * @param nextBricks The list of next bricks to display
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
     * Updates the hold brick display.
     * 
     * @param heldBrick The brick being held (can be null)
     */
    public void updateHoldBrick(Brick heldBrick) {
        if (singlePlayerScreen == null) {
            return;
        }
        
        // Delegate rendering to GameViewRenderer
        renderer.renderHoldBrick(heldBrick, singlePlayerScreen.getHoldBrickRectangles());
    }
    
    /**
     * Gets the current brick data from the screen.
     * 
     * @return The current brick data
     */
    public ViewData getCurrentBrickData() {
        if (singlePlayerScreen == null) {
            return null;
        }
        return singlePlayerScreen.getCurrentBrickData();
    }
    
    /**
     * Sets the current brick data on the screen.
     * 
     * @param brickData The brick data to store
     */
    public void setCurrentBrickData(ViewData brickData) {
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setCurrentBrickData(brickData);
        }
    }
    
    /**
     * Sets the event listener for the single player screen.
     * 
     * @param eventListener The event listener
     */
    public void setEventListener(InputEventListener eventListener) {
        if (singlePlayerScreen != null) {
            singlePlayerScreen.setEventListener(eventListener);
        }
    }
}

