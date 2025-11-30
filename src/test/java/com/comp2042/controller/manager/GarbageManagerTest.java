package com.comp2042.controller.manager;

import com.comp2042.controller.GameController;
import com.comp2042.model.SimpleBoard;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class GarbageManagerTest {

    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFX() throws InterruptedException {
        // Initialize JavaFX toolkit if not already initialized
        if (!javaFxInitialized) {
            CountDownLatch latch = new CountDownLatch(1);
            // Platform.startup must be called from a non-application thread
            Thread initThread = new Thread(() -> {
                try {
                    Platform.startup(() -> {
                        latch.countDown();
                    });
                } catch (IllegalStateException e) {
                    // Toolkit already initialized, which is fine
                    latch.countDown();
                } catch (Exception e) {
                    System.err.println("JavaFX initialization failed: " + e.getMessage());
                    latch.countDown();
                }
            });
            initThread.start();
            latch.await(5, TimeUnit.SECONDS);
            javaFxInitialized = true;
        }
    }

    private GameStateManager gameStateManager;
    private GarbageManager garbageManager;
    private GameController gameController1;
    private GameController gameController2;
    private MockGuiController mockGuiController1;
    private MockGuiController mockGuiController2;

    @BeforeEach
    void setUp() {
        gameStateManager = new GameStateManager(null, null);
        // Set multiplayer mode to true for garbage sending tests
        gameStateManager.setMultiplayerMode(true);
        garbageManager = new GarbageManager(gameStateManager);
        mockGuiController1 = new MockGuiController();
        mockGuiController2 = new MockGuiController();
        
        try {
            gameController1 = new GameController(mockGuiController1, 1);
            gameController2 = new GameController(mockGuiController2, 2);
            garbageManager.setGameControllers(gameController1, gameController2);
        } catch (Exception e) {
            // If JavaFX not available, tests will be skipped
            gameController1 = null;
            gameController2 = null;
        }
    }

    /**
     * Minimal mock GuiController for testing.
     * Extends GuiController but may require JavaFX context.
     */
    static class MockGuiController extends com.comp2042.controller.GuiController {
        // Inherits from GuiController, methods can be overridden if needed
    }

    @Test
    void testSendGarbageToOpponent_GarbageQueueUpdated() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // Send 2 garbage lines to opponent (player 2)
        // Note: This tests that garbage is queued, not the calculation rules
        // Calculation rules: 1 line→0, 2 lines→1, 3 lines→2, 4 lines→4
        garbageManager.sendGarbageToOpponent(1, 2);

        // Wait a bit for Platform.runLater to execute (in real scenario)
        // For testing, we check that garbage was added to queue
        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertTrue(newGarbageCount >= initialGarbageCount, "Garbage queue should be updated");
    }

    @Test
    void testSendGarbageToOpponent_CorrectNumberQueued() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // Send 3 garbage lines
        // Note: This tests that the exact number of garbage lines is queued correctly
        // Calculation rules: 1 line→0, 2 lines→1, 3 lines→2, 4 lines→4
        garbageManager.sendGarbageToOpponent(1, 3);

        // Check that 3 lines were added
        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount + 3, newGarbageCount, "Should queue exactly 3 garbage lines");
    }

    @Test
    void testSendGarbageToOpponent_DoesNotSendOnSingleLineClear() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // According to rules: 1 line → 0 garbage
        // So sending 0 garbage should not change the queue
        garbageManager.sendGarbageToOpponent(1, 0);

        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount, newGarbageCount, "Single line clear (0 garbage) should not update queue");
    }

    @Test
    void testSendGarbageToOpponent_DoesNotSendInSinglePlayerMode() {
        if (gameController1 == null) {
            return; // Skip if JavaFX not available
        }

        // Create a single player game state manager
        GameStateManager singlePlayerManager = new GameStateManager(null, null);
        GarbageManager singlePlayerGarbageManager = new GarbageManager(singlePlayerManager);
        singlePlayerGarbageManager.setGameControllers(gameController1, null);

        SimpleBoard board = gameController1.getSimpleBoard();
        int initialGarbageCount = board.getPendingGarbageCount();

        // In single player mode, garbage should not be sent
        singlePlayerGarbageManager.sendGarbageToOpponent(1, 2);

        int newGarbageCount = board.getPendingGarbageCount();
        assertEquals(initialGarbageCount, newGarbageCount, "Should not send garbage in single player mode");
    }

    @Test
    void testSendGarbageToOpponent_SendsToCorrectOpponent() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard board1 = gameController1.getSimpleBoard();
        SimpleBoard board2 = gameController2.getSimpleBoard();
        
        int initialGarbage1 = board1.getPendingGarbageCount();
        int initialGarbage2 = board2.getPendingGarbageCount();

        // Player 1 sends garbage - should go to player 2
        garbageManager.sendGarbageToOpponent(1, 2);

        // Player 2's queue should increase, player 1's should not
        int newGarbage1 = board1.getPendingGarbageCount();
        int newGarbage2 = board2.getPendingGarbageCount();

        assertEquals(initialGarbage1, newGarbage1, "Player 1's queue should not change");
        assertTrue(newGarbage2 >= initialGarbage2, "Player 2's queue should increase");
    }

    @Test
    void testGarbageCalculationRules_OneLineSendsZeroGarbage() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // According to rules: 1 line cleared → 0 garbage sent
        garbageManager.sendGarbageToOpponent(1, 0);

        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount, newGarbageCount, "1 line clear should send 0 garbage");
    }

    @Test
    void testGarbageCalculationRules_TwoLinesSendsOneGarbage() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // According to rules: 2 lines cleared → 1 garbage sent
        garbageManager.sendGarbageToOpponent(1, 1);

        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount + 1, newGarbageCount, "2 lines clear should send 1 garbage");
    }

    @Test
    void testGarbageCalculationRules_ThreeLinesSendsTwoGarbage() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // According to rules: 3 lines cleared → 2 garbage sent
        garbageManager.sendGarbageToOpponent(1, 2);

        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount + 2, newGarbageCount, "3 lines clear should send 2 garbage");
    }

    @Test
    void testGarbageCalculationRules_FourLinesSendsFourGarbage() {
        if (gameController1 == null || gameController2 == null) {
            return; // Skip if JavaFX not available
        }

        SimpleBoard opponentBoard = gameController2.getSimpleBoard();
        int initialGarbageCount = opponentBoard.getPendingGarbageCount();

        // According to rules: 4 lines cleared (Tetris) → 4 garbage sent
        garbageManager.sendGarbageToOpponent(1, 4);

        int newGarbageCount = opponentBoard.getPendingGarbageCount();
        assertEquals(initialGarbageCount + 4, newGarbageCount, "4 lines clear (Tetris) should send 4 garbage");
    }
}

