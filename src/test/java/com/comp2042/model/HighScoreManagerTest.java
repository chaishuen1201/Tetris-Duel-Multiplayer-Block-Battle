package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HighScoreManager focusing on score management logic.
 */
class HighScoreManagerTest {

    private HighScoreManager highScoreManager;

    @BeforeEach
    void setUp() {
        highScoreManager = new HighScoreManager();
    }

    @Test
    void testAddScore_KeepsTopThreeScores() {
        // Add 5 scores
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        highScoreManager.addScore(300);
        highScoreManager.addScore(400);
        highScoreManager.addScore(500);
        
        List<Integer> topScores = highScoreManager.getTopScores(10);
        
        // Should only keep top 3
        assertEquals(3, topScores.size(), "Should only keep top 3 scores");
        assertEquals(500, topScores.get(0), "Highest score should be first");
        assertEquals(400, topScores.get(1), "Second highest should be second");
        assertEquals(300, topScores.get(2), "Third highest should be third");
    }

    @Test
    void testAddScore_SortsInDescendingOrder() {
        // Add scores in random order
        highScoreManager.addScore(300);
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        
        List<Integer> topScores = highScoreManager.getTopScores(3);
        
        assertEquals(300, topScores.get(0), "Highest score should be first");
        assertEquals(200, topScores.get(1), "Second highest should be second");
        assertEquals(100, topScores.get(2), "Lowest score should be last");
    }

    @Test
    void testAddScore_IgnoresZeroAndNegativeScores() {
        highScoreManager.addScore(100);
        highScoreManager.addScore(0);
        highScoreManager.addScore(-50);
        highScoreManager.addScore(200);
        
        List<Integer> topScores = highScoreManager.getTopScores(10);
        
        // Should only have 2 scores (100 and 200)
        assertEquals(2, topScores.size(), "Should ignore zero and negative scores");
        assertEquals(200, topScores.get(0), "Highest valid score should be first");
        assertEquals(100, topScores.get(1), "Second valid score should be second");
    }

    @Test
    void testGetTopScores_ReturnsCorrectNumberOfScores() {
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        highScoreManager.addScore(300);
        
        // Request top 2
        List<Integer> top2 = highScoreManager.getTopScores(2);
        assertEquals(2, top2.size(), "Should return top 2 scores");
        assertEquals(300, top2.get(0), "First should be highest");
        assertEquals(200, top2.get(1), "Second should be second highest");
        
        // Request top 5 (but only 3 exist)
        List<Integer> top5 = highScoreManager.getTopScores(5);
        assertEquals(3, top5.size(), "Should return only available scores");
    }

    @Test
    void testGetTopScores_ReturnsEmptyListWhenNoScores() {
        List<Integer> topScores = highScoreManager.getTopScores(10);
        
        assertTrue(topScores.isEmpty(), "Should return empty list when no scores exist");
    }

    @Test
    void testAddScore_ReplacesLowestScoreWhenFull() {
        // Fill with 3 scores
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        highScoreManager.addScore(300);
        
        // Add a score higher than the lowest
        highScoreManager.addScore(250);
        
        List<Integer> topScores = highScoreManager.getTopScores(10);
        
        assertEquals(3, topScores.size(), "Should still have 3 scores");
        assertEquals(300, topScores.get(0), "Highest should remain");
        assertEquals(250, topScores.get(1), "New score should be second");
        assertEquals(200, topScores.get(2), "Previous second should be third");
        // 100 should be removed
        assertFalse(topScores.contains(100), "Lowest score should be removed");
    }

    @Test
    void testAddScore_DoesNotAddScoreLowerThanLowestWhenFull() {
        // Fill with 3 scores
        highScoreManager.addScore(100);
        highScoreManager.addScore(200);
        highScoreManager.addScore(300);
        
        // Add a score lower than all existing scores
        highScoreManager.addScore(50);
        
        List<Integer> topScores = highScoreManager.getTopScores(10);
        
        assertEquals(3, topScores.size(), "Should still have 3 scores");
        assertEquals(300, topScores.get(0), "Highest should remain");
        assertEquals(200, topScores.get(1), "Second should remain");
        assertEquals(100, topScores.get(2), "Lowest should remain");
        // 50 should not be added
        assertFalse(topScores.contains(50), "Lower score should not be added");
    }
}

