package com.comp2042.event;

import com.comp2042.model.DownData;
import com.comp2042.model.ViewData;

/**
 * Interface for handling input events in the game.
 * This interface defines the contract for processing all types of game move events
 * including movement (down, left, right), rotation, hard drop, and hold operations.
 * Implementations of this interface (such as GameController) process these events,
 * update the game state, and return updated view data for rendering. The interface
 * also defines a method for resetting the game board. This interface enables the
 * separation of input handling from game logic, allowing different implementations
 * to process events while maintaining a consistent API for the view layer.
 */
public interface InputEventListener {

    /**
     * Handles the down move event, moving the brick down one position.
     * If the brick cannot move down, it merges to the background, clears completed rows,
     * and creates a new brick. Returns DownData which includes information about any
     * lines that were cleared and the updated view data.
     * 
     * @param event The move event containing event type and source information
     * @return DownData containing information about cleared rows and updated view data,
     *         or null if the operation could not be completed
     */
    DownData onDownEvent(MoveEvent event);

    /**
     * Handles the left move event, moving the brick one position to the left.
     * The move is only performed if the new position is valid (no collisions).
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and state after the move
     */
    ViewData onLeftEvent(MoveEvent event);

    /**
     * Handles the right move event, moving the brick one position to the right.
     * The move is only performed if the new position is valid (no collisions).
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and state after the move
     */
    ViewData onRightEvent(MoveEvent event);

    /**
     * Handles the rotate event, rotating the brick 90 degrees counter-clockwise.
     * The rotation is only performed if the rotated position is valid (no collisions).
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick position and rotation state after the rotation
     */
    ViewData onRotateEvent(MoveEvent event);

    /**
     * Handles the hard drop event, instantly dropping the brick to the bottom of the board.
     * The brick is immediately placed at its lowest valid position, merged to the background,
     * and any completed rows are cleared. Awards bonus points based on the number of cells dropped.
     * 
     * @param event The move event containing event type and source information
     * @return DownData containing information about cleared rows and updated view data,
     *         or null if the operation could not be completed
     */
    DownData onHardDropEvent(MoveEvent event);

    /**
     * Handles the hold event, storing the current brick and swapping with the previously held brick.
     * If no brick is currently held, stores the current brick and creates a new one.
     * If a brick is already held, swaps the current brick with the held brick.
     * 
     * @param event The move event containing event type and source information
     * @return ViewData containing the updated brick state after holding (the newly active brick)
     */
    ViewData onHoldEvent(MoveEvent event);

    /**
     * Resets the game board and starts a new game session.
     * Clears the board, resets the score, level, and lines, and creates a new initial brick.
     * This method is called when starting a new game or restarting from a game over state.
     */
    void createNewGame();
}

