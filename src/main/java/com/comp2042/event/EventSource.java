package com.comp2042.event;

/**
 * Enumeration representing the source of a game move event.
 * This enum is used to distinguish between events that originate from user input
 * (keyboard actions) and events that originate from automatic game progression
 * (timeline-driven piece movement). This distinction is important for game logic
 * such as scoring (e.g., soft drop points are only awarded for user-initiated moves)
 * and for debugging/tracking purposes. The enum values are used in MoveEvent objects
 * to track the origin of each game action.
 */
public enum EventSource {
    /**
     * Event originated from user keyboard input.
     * Used when a player presses a key to move, rotate, or drop a piece.
     */
    USER,
    
    /**
     * Event originated from automatic game progression (timer/thread).
     * Used when the game timeline automatically moves a piece down without
     * user intervention.
     */
    THREAD
}

