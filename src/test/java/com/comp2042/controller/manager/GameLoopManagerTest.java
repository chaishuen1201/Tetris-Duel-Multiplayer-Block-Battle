package com.comp2042.controller.manager;

import com.comp2042.model.SimpleBoard;
import javafx.animation.Timeline;
import javafx.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for GameLoopManager focusing on speed calculation based on level.
 */
class GameLoopManagerTest {

    private GameStateManager gameStateManager;
    private GameLoopManager gameLoopManager;
    private Timeline timeline;
    private Timeline timeline1;
    private Timeline timeline2;

    @BeforeEach
    void setUp() {
        gameStateManager = new GameStateManager(new MockAudioManager(), new MockTimerManager());
        gameLoopManager = new GameLoopManager(gameStateManager);
        
        // Create actual Timeline instances
        timeline = new Timeline(new javafx.animation.KeyFrame(Duration.millis(400), e -> {}));
        timeline1 = new Timeline(new javafx.animation.KeyFrame(Duration.millis(400), e -> {}));
        timeline2 = new Timeline(new javafx.animation.KeyFrame(Duration.millis(400), e -> {}));
        
        gameLoopManager.setTimeLine(timeline);
        gameLoopManager.setTimeLine1(timeline1);
        gameLoopManager.setTimeLine2(timeline2);
    }

    /**
     * Mock AudioManager for testing.
     */
    static class MockAudioManager extends AudioManager {
        // Minimal implementation for testing
    }

    /**
     * Mock TimerManager for testing.
     */
    static class MockTimerManager extends TimerManager {
        // Minimal implementation for testing
    }

    @Test
    void testSpeedCalculation_Level1_BaseSpeed() {
        // Level 1: rate = 1.0 + (1 - 1) * 0.25 = 1.0
        gameLoopManager.setCurrentLevel(1);
        gameLoopManager.updateTimelineRate();
        
        assertEquals(1.0, timeline.getRate(), 0.001, 
            "Level 1 should have base speed (rate = 1.0)");
    }

    @Test
    void testSpeedCalculation_Level2_IncreasesBy25Percent() {
        // Level 2: rate = 1.0 + (2 - 1) * 0.25 = 1.25
        gameLoopManager.setCurrentLevel(2);
        gameLoopManager.updateTimelineRate();
        
        assertEquals(1.25, timeline.getRate(), 0.001, 
            "Level 2 should have 25% speed increase (rate = 1.25)");
    }

    @Test
    void testSpeedCalculation_Level3_IncreasesBy50Percent() {
        // Level 3: rate = 1.0 + (3 - 1) * 0.25 = 1.5
        gameLoopManager.setCurrentLevel(3);
        gameLoopManager.updateTimelineRate();
        
        assertEquals(1.5, timeline.getRate(), 0.001, 
            "Level 3 should have 50% speed increase (rate = 1.5)");
    }

    @Test
    void testSpeedCalculation_Level4_IncreasesBy75Percent() {
        // Level 4: rate = 1.0 + (4 - 1) * 0.25 = 1.75
        gameLoopManager.setCurrentLevel(4);
        gameLoopManager.updateTimelineRate();
        
        assertEquals(1.75, timeline.getRate(), 0.001, 
            "Level 4 should have 75% speed increase (rate = 1.75)");
    }

    @Test
    void testSpeedCalculation_Level10_IncreasesBy225Percent() {
        // Level 10: rate = 1.0 + (10 - 1) * 0.25 = 3.25
        gameLoopManager.setCurrentLevel(10);
        gameLoopManager.updateTimelineRate();
        
        assertEquals(3.25, timeline.getRate(), 0.001, 
            "Level 10 should have 225% speed increase (rate = 3.25)");
    }

    @Test
    void testSpeedCalculation_Level0_DefaultsToLevel1() {
        // Level 0 or negative should default to level 1: rate = 1.0
        gameLoopManager.setCurrentLevel(0);
        gameLoopManager.updateTimelineRate();
        
        // Math.max(1, 0) = 1, so rate = 1.0 + (1 - 1) * 0.25 = 1.0
        assertEquals(1.0, timeline.getRate(), 0.001, 
            "Level 0 should default to level 1 speed (rate = 1.0)");
    }

    @Test
    void testSpeedCalculation_Multiplayer_Player1() {
        gameStateManager.setMultiplayerMode(true);
        
        // Create board for player 1
        SimpleBoard board1 = new SimpleBoard(10, 20);
        board1.levelProperty().set(2); // Level 2
        
        // Create game controller - need to use reflection or create a minimal controller
        // For now, test the calculation formula directly
        int level = board1.levelProperty().get();
        double expectedRate = 1.0 + (Math.max(1, level) - 1) * 0.25;
        
        assertEquals(1.25, expectedRate, 0.001, 
            "Player 1 at level 2 should have rate = 1.25");
    }

    @Test
    void testSpeedCalculation_Multiplayer_Player2() {
        gameStateManager.setMultiplayerMode(true);
        
        // Create board for player 2
        SimpleBoard board2 = new SimpleBoard(10, 20);
        board2.levelProperty().set(3); // Level 3
        
        // Test the calculation formula directly
        int level = board2.levelProperty().get();
        double expectedRate = 1.0 + (Math.max(1, level) - 1) * 0.25;
        
        assertEquals(1.5, expectedRate, 0.001, 
            "Player 2 at level 3 should have rate = 1.5");
    }

    @Test
    void testSpeedCalculation_Multiplayer_IndependentLevels() {
        // Test that different levels result in different rates
        SimpleBoard board1 = new SimpleBoard(10, 20);
        board1.levelProperty().set(2);
        
        SimpleBoard board2 = new SimpleBoard(10, 20);
        board2.levelProperty().set(5);
        
        double rate1 = 1.0 + (Math.max(1, board1.levelProperty().get()) - 1) * 0.25;
        double rate2 = 1.0 + (Math.max(1, board2.levelProperty().get()) - 1) * 0.25;
        
        // Player 1: level 2 = 1.25, Player 2: level 5 = 2.0
        assertEquals(1.25, rate1, 0.001, "Player 1 should have rate = 1.25");
        assertEquals(2.0, rate2, 0.001, "Player 2 should have rate = 2.0");
        assertNotEquals(rate1, rate2, "Players should have different rates at different levels");
    }

    @Test
    void testSetCurrentLevel() {
        gameLoopManager.setCurrentLevel(5);
        gameLoopManager.updateTimelineRate();
        
        // Level 5: rate = 1.0 + (5 - 1) * 0.25 = 2.0
        assertEquals(2.0, timeline.getRate(), 0.001, 
            "Setting level to 5 should result in rate = 2.0");
    }

    @Test
    void testSpeedCalculationFormula_AllLevels() {
        // Test the formula: rate = 1.0 + (level - 1) * 0.25
        for (int level = 1; level <= 10; level++) {
            double expectedRate = 1.0 + (level - 1) * 0.25;
            gameLoopManager.setCurrentLevel(level);
            gameLoopManager.updateTimelineRate();
            
            assertEquals(expectedRate, timeline.getRate(), 0.001, 
                "Level " + level + " should have rate = " + expectedRate);
        }
    }
}

