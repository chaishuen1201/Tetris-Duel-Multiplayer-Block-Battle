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
 * Handles sending garbage to opponents when lines are cleared and processing
 * garbage queues for each player.
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
     * Callback interface for triggering game over.
     */
    public interface GameOverCallback {
        void gameOver(int playerNumber);
    }
    
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
     * Sets the game controllers for both players.
     */
    public void setGameControllers(GameController gameController1, GameController gameController2) {
        this.gameController1 = gameController1;
        this.gameController2 = gameController2;
    }
    
    /**
     * Sets the multiplayer screen reference.
     */
    public void setMultiplayerScreen(MultiplayerScreen multiplayerScreen) {
        this.multiplayerScreen = multiplayerScreen;
    }
    
    /**
     * Sets the single player screen reference.
     */
    public void setSinglePlayerScreen(SinglePlayerScreen singlePlayerScreen) {
        this.singlePlayerScreen = singlePlayerScreen;
    }
    
    /**
     * Sets the single player view manager reference.
     */
    public void setSinglePlayerViewManager(SinglePlayerViewManager singlePlayerViewManager) {
        this.singlePlayerViewManager = singlePlayerViewManager;
    }
    
    /**
     * Sets the game over callback.
     */
    public void setGameOverCallback(GameOverCallback gameOverCallback) {
        this.gameOverCallback = gameOverCallback;
    }
    
    /**
     * Sends garbage to the opponent's queue when lines are cleared in multiplayer mode.
     * @param fromPlayerNumber The player number who cleared lines (1 or 2)
     * @param numGarbageLines Number of garbage lines to send
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
     * Processes garbage queue for a player, adding pending garbage lines to the board.
     * This should be called periodically or after certain game events.
     * @param playerNumber The player number (1 or 2)
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
        
        // Only process if there's pending garbage
        if (board.getPendingGarbageCount() > 0) {
            // Process one garbage line at a time to give player time to react
            boolean potentialGameOver = board.processGarbageQueue();
            
            // Refresh the display
            if (gameStateManager.isMultiplayerMode() && playerNumber > 0 && multiplayerScreen != null) {
                renderer.refreshGameBackground(multiplayerScreen, board.getBoardMatrix(), playerNumber);
            } else if (singlePlayerViewManager != null) {
                singlePlayerViewManager.refreshGameBackground(board.getBoardMatrix());
            }
            
            // Check if game over after adding garbage
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
}

