package com.comp2042.view;

import com.comp2042.event.InputEventListener;
import com.comp2042.model.ViewData;
import javafx.beans.property.IntegerProperty;

/**
 * Interface for game view components that display the game state.
 * Defines methods for initializing the view, refreshing the display,
 * binding score properties, and handling game state changes.
 */
public interface GameView {
    /**
     * Initializes the game view with the initial board state and brick.
     * 
     * @param boardMatrix The initial board state matrix
     * @param brick The initial brick view data
     */
    void initGameView(int[][] boardMatrix, ViewData brick);
    
    /**
     * Refreshes the game background display with the updated board state.
     * 
     * @param board The updated board state matrix
     */
    void refreshGameBackground(int[][] board);
    
    /**
     * Refreshes the current brick display.
     * 
     * @param brick The updated brick view data
     */
    void refreshBrick(ViewData brick);
    
    /**
     * Sets the event listener for handling game events.
     * 
     * @param eventListener The InputEventListener to handle game events
     */
    void setEventListener(InputEventListener eventListener);
    
    /**
     * Binds the score property to update the score display.
     * 
     * @param integerProperty The IntegerProperty representing the score
     */
    void bindScore(IntegerProperty integerProperty);
    
    /**
     * Handles the game over state and updates the display accordingly.
     */
    void gameOver();
    
    /**
     * Resets the view for a new game session.
     */
    void newGame();
}

