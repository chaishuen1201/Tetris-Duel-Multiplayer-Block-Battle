package com.comp2042.event;

/**
 * Immutable data class representing a game move event.
 * This class encapsulates information about a game action, including the type of move
 * (DOWN, LEFT, RIGHT, ROTATE, HARD_DROP, HOLD) and the source of the event (USER input
 * or THREAD/automatic progression). The class is immutable to ensure thread safety and
 * prevent accidental modification of event data. MoveEvent objects are used throughout
 * the game system to pass move information from input handlers to game controllers and
 * event listeners, enabling the separation of input processing from game logic.
 */
public final class MoveEvent {
    private final EventType eventType;
    private final EventSource eventSource;

    /**
     * Creates a new MoveEvent with the specified type and source.
     * 
     * @param eventType The type of move event (DOWN, LEFT, RIGHT, ROTATE, HARD_DROP, or HOLD)
     * @param eventSource The source of the event (USER for keyboard input, THREAD for automatic progression)
     */
    public MoveEvent(EventType eventType, EventSource eventSource) {
        this.eventType = eventType;
        this.eventSource = eventSource;
    }

    /**
     * Gets the type of move event.
     * 
     * @return The EventType enum value representing the move direction or action
     *         (DOWN, LEFT, RIGHT, ROTATE, HARD_DROP, or HOLD)
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Gets the source of the move event.
     * 
     * @return The EventSource enum value indicating whether the event came from
     *         user input (USER) or automatic game progression (THREAD)
     */
    public EventSource getEventSource() {
        return eventSource;
    }
}

