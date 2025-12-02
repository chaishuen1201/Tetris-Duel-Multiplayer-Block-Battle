package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.model.SimpleBoard;
import com.comp2042.model.ViewData;
import com.comp2042.util.MatrixOperations;
import com.comp2042.view.MultiplayerScreen;
import com.comp2042.view.SinglePlayerScreen;
import com.comp2042.view.GameViewRenderer;
import javafx.application.Platform;

/**
 * Manages garbage block sending and processing in multiplayer mode.
 * This class coordinates the attack mechanism where players send garbage lines
 * to their opponents when they clear lines. It handles queuing garbage blocks,
 * processing them at appropriate intervals, adding them to the opponent's board,
 * and detecting game over conditions when garbage blocks reach the top of the board.
 * The manager integrates with game controllers, boards, view renderers, and audio
 * to provide a complete garbage block attack system for competitive multiplayer gameplay.
 */
public class GarbageManager {
    
    private final GameStateManager gameStateManager;
    private AudioManager audioManager;
    private GameController gameController1;
    private GameController gameController2;
    private MultiplayerScreen multiplayerScreen;
    private SinglePlayerScreen singlePlayerScreen;
    private SinglePlayerViewManager singlePlayerViewManager;
    private GameOverCallback gameOverCallback;
    private final GameViewRenderer renderer = new GameViewRenderer();
    
    /**
     * Callback interface for triggering game over when garbage blocks cause a player to lose.
     */
    public interface GameOverCallback {
        /**
         * Called when a player's game is over due to garbage blocks.
         * 
         * @param playerNumber The player number (1 or 2) whose game is over
         */
        void gameOver(int playerNumber);
    }
    
    /**
     * Creates a new GarbageManager with the specified game state manager.
     * 
     * @param gameStateManager The game state manager for checking game state and mode
     */
    public GarbageManager(GameStateManager gameStateManager) {
        this.gameStateManager = gameStateManager;
    }
    
    /**
     * Sets the audio manager for playing sounds.
     * @param audioManager The audio manager to use
     */
    public void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }
    
    /**
     * Sets the game controllers for both players in multiplayer mode.
     * 
     * @param gameController1 The GameController for player 1
     * @param gameController2 The GameController for player 2
     */
    public void setGameControllers(GameController gameController1, GameController gameController2) {
        this.gameController1 = gameController1;
        this.gameController2 = gameController2;
    }
    
    /**
     * Sets the multiplayer screen reference for rendering updates.
     * 
     * @param multiplayerScreen The MultiplayerScreen instance for multiplayer mode
     */
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    /**
     * Sets the single player screen reference.
     * 
     * @param singlePlayerScreen The SinglePlayerScreen instance (currently unused but kept for consistency)
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Sets the single player view manager reference for rendering updates.
     * 
     * @param singlePlayerViewManager The SinglePlayerViewManager instance for single player mode
     */
    public void setSinglePlayerViewManager(SinglePlayerViewManager singlePlayerViewManager) {
        this.singlePlayerViewManager = singlePlayerViewManager;
    }
    
    /**
     * Sets the callback to be invoked when garbage blocks cause a game over condition.
     * 
     * @param gameOverCallback The GameOverCallback implementation to handle game over events
     */
    public void setGameOverCallback(GameOverCallback gameOverCallback) {
        this.gameOverCallback = gameOverCallback;
    }
    
    /**
     * Sends garbage lines to the opponent's queue when lines are cleared in multiplayer mode.
     * This is the attack mechanism where clearing lines sends garbage blocks to the opponent.
     * The garbage is added to the opponent's queue and processed immediately to ensure
     * it appears right away. Plays a garbage sound effect when garbage is sent.
     * 
     * @param fromPlayerNumber The player number who cleared lines (1 or 2)
     * @param numGarbageLines Number of garbage lines to send to the opponent
     */
    public void sendGarbageToOpponent(int fromPlayerNumber, int numGarbageLines) {
        if (!gameStateManager.isMultiplayerMode() || numGarbageLines <= 0) {
            return;
        }
        
        // Play garbage sound when garbage is sent
        if (audioManager != null) {
            audioManager.playGarbage();
        }
        
        // Determine opponent's player number
        int opponentNumber = (fromPlayerNumber == 1) ? 2 : 1;
        
        // Get the opponent's game controller
        GameController opponentController = (opponentNumber == 1) ? gameController1 : gameController2;
        
        if (opponentController != null) {
            SimpleBoard opponentBoard = opponentController.getSimpleBoard();
            if (opponentBoard != null) {
                opponentBoard.addGarbageToQueue(numGarbageLines);
                // Process garbage immediately instead of waiting for timeline
                // This ensures garbage appears right away
                Platform.runLater(() -> {
                    if (opponentBoard.getPendingGarbageCount() > 0) {
                        processGarbageQueue(opponentNumber);
                    }
                });
            }
        }
    }
    
    /**
     * Processes the garbage queue for a player, adding pending garbage lines to the board.
     * Processes all pending garbage lines at once to avoid delays when multiple lines
     * are sent together. After processing, refreshes the game display and checks for
     * game over conditions. The method checks if the game is paused or already over
     * before processing, and verifies that the current brick position is blocked
     * before triggering game over.
     * 
     * @param playerNumber The player number (1 or 2) whose garbage queue should be processed
     */
    public void processGarbageQueue(int playerNumber) {
        if (!gameStateManager.isMultiplayerMode() || playerNumber <= 0) {
            return;
        }
        
        boolean isGameOver = (playerNumber == 1) ? gameStateManager.isGameOver1() : gameStateManager.isGameOver2();
        if (isGameOver || gameStateManager.isPaused()) {
            return;
        }
        
        GameController controller = (playerNumber == 1) ? gameController1 : gameController2;
        if (controller == null) {
            return;
        }
        
        SimpleBoard board = controller.getSimpleBoard();
        if (board == null) {
            return;
        }
        
        // Process all pending garbage lines at once to avoid delays
        boolean potentialGameOver = false;
        while (board.getPendingGarbageCount() > 0) {
            boolean gameOver = board.processGarbageQueue();
            if (gameOver) {
                potentialGameOver = true;
                // Continue processing remaining garbage to ensure all lines are added
            }
        }
        
        // Refresh the display after processing all garbage
        if (board.getPendingGarbageCount() == 0) {
            if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
                renderer.refreshGameBackground(multiplayerScreen, board.getBoardMatrix(), playerNumber);
            } else if (singlePlayerViewManager != null) {
                singlePlayerViewManager.refreshGameBackground(board.getBoardMatrix());
            }
        }
        
        // Check if game over after adding all garbage
        if (potentialGameOver) {
            // Get current view data to check brick position
            ViewData viewData = board.getViewData();
            if (viewData != null) {
                // Check if the current brick position is blocked
                if (MatrixOperations.intersect(board.getBoardMatrix(), 
                        viewData.getBrickData(), 
                        viewData.getXPosition(), 
                        viewData.getYPosition())) {
                    if (gameOverCallback != null) {
                        gameOverCallback.gameOver(playerNumber);
                    }
                }
            }
        }
    }
}

