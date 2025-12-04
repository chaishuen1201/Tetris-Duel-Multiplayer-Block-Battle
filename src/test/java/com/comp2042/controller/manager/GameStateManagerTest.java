package com.comp2042.controller.manager;

import javafx.beans.property.BooleanProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameStateManager focusing on state transitions and multiplayer winner logic.
 */
class GameStateManagerTest {

    private GameStateManager gameStateManager;
    private MockAudioManager mockAudioManager;
    private MockTimerManager mockTimerManager;
    private boolean gameOverPanelShown;

    @BeforeEach
    void setUp() {
        mockAudioManager = new MockAudioManager();
        mockTimerManager = new MockTimerManager();
        gameStateManager = new GameStateManager(mockAudioManager, mockTimerManager);
        gameOverPanelShown = false;
    }

    /**
     * Mock AudioManager for testing.
     */
    static class MockAudioManager extends AudioManager {
        private boolean gameOverPlayed = false;
        private boolean winnerPlayed = false;
        private boolean gameMusicStopped = false;

        @Override
        public void playGameOver() {
            gameOverPlayed = true;
        }

        @Override
        public void playWinner() {
            winnerPlayed = true;
        }

        @Override
        public void stopGameMusic() {
            gameMusicStopped = true;
        }

        public boolean isGameOverPlayed() {
            return gameOverPlayed;
        }

        public boolean isWinnerPlayed() {
            return winnerPlayed;
        }

        public boolean isGameMusicStopped() {
            return gameMusicStopped;
        }

        public void reset() {
            gameOverPlayed = false;
            winnerPlayed = false;
            gameMusicStopped = false;
        }
    }

    /**
     * Mock TimerManager for testing.
     * Overrides all methods that use JavaFX to avoid initialization issues.
     */
    static class MockTimerManager extends TimerManager {
        private int multiplayerElapsedSeconds = 120;

        @Override
        public int getMultiplayerElapsedSeconds() {
            return multiplayerElapsedSeconds;
        }

        public void setMultiplayerElapsedSeconds(int seconds) {
            this.multiplayerElapsedSeconds = seconds;
        }

        @Override
        public void startSinglePlayerTimer() {
            // Don't call super to avoid JavaFX Timeline initialization
            // Just track that this was called
        }

        @Override
        public void stopSinglePlayerTimer() {
            // No-op for testing
        }

        @Override
        public void pauseSinglePlayerTimer() {
            // No-op for testing
        }

        @Override
        public void resumeSinglePlayerTimer() {
            // No-op for testing
        }

        @Override
        public void resetSinglePlayerTimer() {
            // No-op for testing
        }

        @Override
        public void startMultiplayerTimer() {
            // No-op for testing
        }

        @Override
        public void stopMultiplayerTimer() {
            // No-op for testing
        }

        @Override
        public void resetMultiplayerTimer() {
            // No-op for testing
        }
    }

    @Test
    void testMultiplayerWinner_Player1Loses_Player2Wins() {
        // Setup multiplayer mode
        gameStateManager.setMultiplayerMode(true);
        // Use actuallyStartGame() to set gameStarted flag
        gameStateManager.actuallyStartGame();
        
        mockTimerManager.setMultiplayerElapsedSeconds(150);
        
        // Player 1 loses
        gameStateManager.gameOver(1);
        
        // Player 2 should win - check that both are marked game over and winner sound plays
        assertTrue(gameStateManager.isGameOver1(), "Player 1 should be game over");
        assertTrue(gameStateManager.isGameOver2(), "Player 2 should also be marked game over (winner)");
        assertTrue(mockAudioManager.isWinnerPlayed(), "Winner sound should play");
        assertTrue(mockAudioManager.isGameMusicStopped(), "Game music should stop");
    }

    @Test
    void testMultiplayerWinner_Player2Loses_Player1Wins() {
        // Setup multiplayer mode
        gameStateManager.setMultiplayerMode(true);
        gameStateManager.actuallyStartGame();
        
        mockTimerManager.setMultiplayerElapsedSeconds(200);
        
        // Player 2 loses
        gameStateManager.gameOver(2);
        
        // Player 1 should win
        assertTrue(gameStateManager.isGameOver2(), "Player 2 should be game over");
        assertTrue(gameStateManager.isGameOver1(), "Player 1 should also be marked game over (winner)");
        assertTrue(mockAudioManager.isWinnerPlayed(), "Winner sound should play");
    }

    @Test
    void testMultiplayerTie_BothPlayersLose() {
        // Setup multiplayer mode
        gameStateManager.setMultiplayerMode(true);
        gameStateManager.actuallyStartGame();
        
        // Both players lose - call gameOver for both
        gameStateManager.gameOver(1);
        mockAudioManager.reset(); // Reset after first game over
        
        // Now player 2 also loses (both are already game over)
        gameStateManager.gameOver(2);
        
        // Should be a tie - both game over, game over sound (not winner sound)
        assertTrue(gameStateManager.isGameOver1(), "Player 1 should be game over");
        assertTrue(gameStateManager.isGameOver2(), "Player 2 should be game over");
        assertTrue(mockAudioManager.isGameOverPlayed(), "Game over sound should play for tie");
        assertFalse(mockAudioManager.isWinnerPlayed(), "Winner sound should not play for tie");
    }

    @Test
    void testSinglePlayerGameOver() {
        // Setup single player mode
        gameStateManager.setMultiplayerMode(false);
        gameStateManager.actuallyStartGame();
        
        // Setup game over panel callback
        gameStateManager.setOnShowGameOverPanel(() -> {
            gameOverPanelShown = true;
        });
        
        // Game over
        gameStateManager.gameOver(0);
        
        // Should show game over panel
        assertTrue(gameStateManager.isGameOver(), "Game should be over");
        assertTrue(gameOverPanelShown, "Game over panel should be shown");
        assertTrue(mockAudioManager.isGameOverPlayed(), "Game over sound should play");
        assertTrue(mockAudioManager.isGameMusicStopped(), "Game music should stop");
    }

    @Test
    void testPauseResume_SinglePlayer() {
        gameStateManager.setMultiplayerMode(false);
        gameStateManager.actuallyStartGame();
        
        // Initially not paused
        assertFalse(gameStateManager.isPaused(), "Game should not be paused initially");
        
        // Pause
        gameStateManager.pauseGame();
        assertTrue(gameStateManager.isPaused(), "Game should be paused");
        
        // Resume
        gameStateManager.pauseGame();
        assertFalse(gameStateManager.isPaused(), "Game should be resumed");
    }

    @Test
    void testPauseResume_Multiplayer() {
        gameStateManager.setMultiplayerMode(true);
        gameStateManager.startMultiplayerGame();
        
        // Initially not paused
        assertFalse(gameStateManager.isPaused(), "Game should not be paused initially");
        
        // Pause
        gameStateManager.pauseGame();
        assertTrue(gameStateManager.isPaused(), "Game should be paused");
        
        // Resume
        gameStateManager.pauseGame();
        assertFalse(gameStateManager.isPaused(), "Game should be resumed");
    }

    @Test
    void testGameStateFlags_InitialState() {
        assertFalse(gameStateManager.isPaused(), "Should not be paused initially");
        assertFalse(gameStateManager.isGameOver(), "Should not be game over initially");
        assertFalse(gameStateManager.isGameOver1(), "Player 1 should not be game over initially");
        assertFalse(gameStateManager.isGameOver2(), "Player 2 should not be game over initially");
        assertFalse(gameStateManager.isGameStarted(), "Game should not be started initially");
        assertFalse(gameStateManager.isMultiplayerMode(), "Should not be multiplayer mode initially");
    }

    @Test
    void testSetMultiplayerMode() {
        gameStateManager.setMultiplayerMode(true);
        assertTrue(gameStateManager.isMultiplayerMode(), "Should be in multiplayer mode");
        
        gameStateManager.setMultiplayerMode(false);
        assertFalse(gameStateManager.isMultiplayerMode(), "Should not be in multiplayer mode");
    }

    @Test
    void testGameStarted_AfterActuallyStartGame() {
        assertFalse(gameStateManager.isGameStarted(), "Game should not be started initially");
        
        gameStateManager.actuallyStartGame();
        assertTrue(gameStateManager.isGameStarted(), "Game should be started after actuallyStartGame()");
    }

    @Test
    void testStateProperties_AreReactive() {
        BooleanProperty pauseProperty = gameStateManager.isPauseProperty();
        BooleanProperty gameOverProperty = gameStateManager.isGameOverProperty();
        BooleanProperty gameOver1Property = gameStateManager.isGameOver1Property();
        BooleanProperty gameOver2Property = gameStateManager.isGameOver2Property();
        
        assertNotNull(pauseProperty, "Pause property should not be null");
        assertNotNull(gameOverProperty, "Game over property should not be null");
        assertNotNull(gameOver1Property, "Game over 1 property should not be null");
        assertNotNull(gameOver2Property, "Game over 2 property should not be null");
        
        // Test that properties are reactive
        pauseProperty.set(true);
        assertTrue(gameStateManager.isPaused(), "Pause state should update");
        
        gameOverProperty.set(true);
        assertTrue(gameStateManager.isGameOver(), "Game over state should update");
    }
}

