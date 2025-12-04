package com.comp2042.model;

/**
 * Interface representing the game board for Tetris.
 * Defines operations for moving and rotating bricks, managing game state,
 * clearing rows, and tracking score. Implementations manage the game board
 * matrix, current brick state, and game logic.
 */
public interface Board {

    /**
     * Moves the current brick down one position.
     * 
     * @return True if the brick was moved successfully, false if it cannot move down
     */
    boolean moveBrickDown();

    /**
     * Moves the current brick one position to the left.
     * 
     * @return True if the brick was moved successfully, false if it cannot move left
     */
    boolean moveBrickLeft();

    /**
     * Moves the current brick one position to the right.
     * 
     * @return True if the brick was moved successfully, false if it cannot move right
     */
    boolean moveBrickRight();

    /**
     * Rotates the current brick 90 degrees counter-clockwise.
     * 
     * @return True if the brick was rotated successfully, false if rotation is not possible
     */
    boolean rotateLeftBrick();

    /**
     * Creates a new brick at the top of the board.
     * 
     * @return True if a new brick was created successfully, false if the game is over (board is full)
     */
    boolean createNewBrick();

    /**
     * Gets the current state of the game board matrix.
     * 
     * @return A 2D array representing the board state, where 0 represents empty cells
     */
    int[][] getBoardMatrix();

    /**
     * Gets the view data for the current brick including position and shape.
     * 
     * @return ViewData containing the current brick's position and shape information
     */
    ViewData getViewData();

    /**
     * Merges the current brick into the background board matrix.
     * This is called when the brick can no longer move down.
     */
    void mergeBrickToBackground();

    /**
     * Clears completed rows from the board and returns information about the clear operation.
     * 
     * @return ClearRow object containing the number of lines removed and score bonus
     */
    ClearRow clearRows();

    /**
     * Gets the score object for tracking game score.
     * 
     * @return The Score object representing the current game score
     */
    Score getScore();

    /**
     * Resets the board to start a new game.
     * Clears the board matrix and resets the score.
     */
    void newGame();
}

