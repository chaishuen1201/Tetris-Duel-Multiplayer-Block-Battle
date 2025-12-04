package com.comp2042.util;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for KeyBindingsManager focusing on multiplayer key binding conflicts.
 */
class KeyBindingsManagerTest {

    private KeyBindingsManager keyBindingsManager;

    @BeforeEach
    void setUp() {
        keyBindingsManager = KeyBindingsManager.getInstance();
    }

    @Test
    void testGetConflictingPlayerMode_Player1KeyBoundToPlayer2_ReturnsPlayer2() {
        // Set a key binding for Player 2
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER2,
            KeyBindingsManager.Action.LEFT,
            KeyCode.A
        );

        // Check if Player 1 trying to use the same key detects conflict
        KeyBindingsManager.PlayerMode conflict = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.A,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertEquals(KeyBindingsManager.PlayerMode.PLAYER2, conflict, 
            "Should detect Player 2 as conflicting when Player 1 tries to bind Player 2's key");
    }

    @Test
    void testGetConflictingPlayerMode_Player2KeyBoundToPlayer1_ReturnsPlayer1() {
        // Set a key binding for Player 1
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.RIGHT,
            KeyCode.D
        );

        // Check if Player 2 trying to use the same key detects conflict
        KeyBindingsManager.PlayerMode conflict = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.D,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertEquals(KeyBindingsManager.PlayerMode.PLAYER1, conflict, 
            "Should detect Player 1 as conflicting when Player 2 tries to bind Player 1's key");
    }

    @Test
    void testGetConflictingPlayerMode_NoConflict_ReturnsNull() {
        // Set a key binding for Player 1
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.LEFT,
            KeyCode.A
        );

        // Player 2 tries to use a different key (no conflict)
        KeyBindingsManager.PlayerMode conflict = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.B,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertNull(conflict, "Should return null when there is no conflict");
    }

    @Test
    void testGetConflictingPlayerMode_SinglePlayerMode_ReturnsNull() {
        // Set a key binding for SINGLE player mode
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.SINGLE,
            KeyBindingsManager.Action.LEFT,
            KeyCode.A
        );

        // Player 1 tries to use the same key - should not conflict with SINGLE mode
        KeyBindingsManager.PlayerMode conflict = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.A,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertNull(conflict, "SINGLE player mode should not conflict with PLAYER1 or PLAYER2");
    }

    @Test
    void testGetActionInPlayerMode_ReturnsCorrectAction() {
        // Set a key binding for Player 1
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.ROTATE,
            KeyCode.Q
        );

        // Get the action for this key in Player 1 mode
        KeyBindingsManager.Action action = keyBindingsManager.getActionInPlayerMode(
            KeyCode.Q,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertEquals(KeyBindingsManager.Action.ROTATE, action, 
            "Should return the correct action for the key in the specified player mode");
    }

    @Test
    void testGetActionInPlayerMode_DifferentPlayerMode_ReturnsNull() {
        // Set a key binding for Player 1
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.ROTATE,
            KeyCode.Q
        );

        // Get the action for this key in Player 2 mode (should be null)
        KeyBindingsManager.Action action = keyBindingsManager.getActionInPlayerMode(
            KeyCode.Q,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertNull(action, "Should return null when key is not bound in the specified player mode");
    }

    @Test
    void testIsKeyBound_ReturnsTrueWhenBound() {
        // Set a key binding
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.SOFT_DROP,
            KeyCode.S
        );

        // Check if key is bound
        boolean isBound = keyBindingsManager.isKeyBound(
            KeyCode.S,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertTrue(isBound, "Should return true when key is bound to an action");
    }

    @Test
    void testIsKeyBound_ReturnsFalseWhenNotBound() {
        // Check if key is bound (without setting it)
        boolean isBound = keyBindingsManager.isKeyBound(
            KeyCode.X,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertFalse(isBound, "Should return false when key is not bound to any action");
    }

    @Test
    void testConflictDetection_MultipleActions_SameKey() {
        // Set multiple actions for Player 1 using different keys
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.LEFT,
            KeyCode.A
        );
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.RIGHT,
            KeyCode.D
        );

        // Player 2 tries to use Player 1's keys
        KeyBindingsManager.PlayerMode conflict1 = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.A,
            KeyBindingsManager.PlayerMode.PLAYER2
        );
        KeyBindingsManager.PlayerMode conflict2 = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.D,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertEquals(KeyBindingsManager.PlayerMode.PLAYER1, conflict1, 
            "Should detect conflict for first key");
        assertEquals(KeyBindingsManager.PlayerMode.PLAYER1, conflict2, 
            "Should detect conflict for second key");
    }

    @Test
    void testConflictDetection_Player1AndPlayer2CanHaveDifferentKeys() {
        // Set different keys for Player 1 and Player 2
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER1,
            KeyBindingsManager.Action.LEFT,
            KeyCode.A
        );
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER2,
            KeyBindingsManager.Action.LEFT,
            KeyCode.LEFT
        );

        // No conflict should be detected
        KeyBindingsManager.PlayerMode conflict1 = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.A,
            KeyBindingsManager.PlayerMode.PLAYER1
        );
        KeyBindingsManager.PlayerMode conflict2 = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.LEFT,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertNull(conflict1, "Player 1 should not conflict with itself");
        assertNull(conflict2, "Player 2 should not conflict with itself");
        
        // But Player 1 trying to use Player 2's key should conflict
        KeyBindingsManager.PlayerMode conflict3 = keyBindingsManager.getConflictingPlayerMode(
            KeyCode.LEFT,
            KeyBindingsManager.PlayerMode.PLAYER1
        );
        assertEquals(KeyBindingsManager.PlayerMode.PLAYER2, conflict3, 
            "Player 1 should detect conflict when trying to use Player 2's key");
    }

    @Test
    void testGetActionForKey_ReturnsCorrectAction() {
        // Set a key binding
        keyBindingsManager.setKeyBinding(
            KeyBindingsManager.PlayerMode.PLAYER2,
            KeyBindingsManager.Action.HARD_DROP,
            KeyCode.SPACE
        );

        // Get the action for this key
        KeyBindingsManager.Action action = keyBindingsManager.getActionForKey(
            KeyCode.SPACE,
            KeyBindingsManager.PlayerMode.PLAYER2
        );

        assertEquals(KeyBindingsManager.Action.HARD_DROP, action, 
            "Should return the correct action for the key");
    }

    @Test
    void testGetActionForKey_ReturnsNullWhenNotBound() {
        // Get action for unbound key
        KeyBindingsManager.Action action = keyBindingsManager.getActionForKey(
            KeyCode.Z,
            KeyBindingsManager.PlayerMode.PLAYER1
        );

        assertNull(action, "Should return null when key is not bound to any action");
    }
}

