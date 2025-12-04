package com.comp2042.event;

/**
 * Enumeration representing the types of game move events.
 * This enum defines all possible move actions that can be performed on game pieces
 * during gameplay. Each event type represents a specific player action or automatic
 * game progression action. The enum values are used in MoveEvent objects to specify
 * what type of action should be executed. The event types cover all standard Tetris
 * gameplay actions including movement (down, left, right), rotation, hard drop, and
 * hold functionality.
 */
public enum EventType {
    /**
     * Move the brick down one position.
     * Used for both automatic downward movement (timeline-driven) and user-initiated
     * soft drop actions.
     */
    DOWN,
    
    /**
     * Move the brick one position to the left.
     * Used when the player presses the left movement key.
     */
    LEFT,
    
    /**
     * Move the brick one position to the right.
     * Used when the player presses the right movement key.
     */
    RIGHT,
    
    /**
     * Rotate the brick 90 degrees counter-clockwise.
     * Used when the player presses the rotate key to spin the current piece.
     */
    ROTATE,
    
    /**
     * Instantly drop the brick to the bottom of the board.
     * Used when the player performs a hard drop action, immediately placing the
     * piece at its lowest valid position and awarding bonus points.
     */
    HARD_DROP,
    
    /**
     * Hold the current brick and swap with the previously held brick.
     * Used when the player presses the hold key to store the current piece and
     * retrieve a previously held piece (or create a new one if no piece was held).
     */
    HOLD
}

