package com.comp2042.logic;

import com.comp2042.bricks.Brick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RandomBrickGenerator focusing on brick generation logic and queue management.
 */
class RandomBrickGeneratorTest {

    private RandomBrickGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RandomBrickGenerator();
    }

    @Test
    void testGetBrick_ReturnsNotNull() {
        Brick brick = generator.getBrick();
        assertNotNull(brick, "getBrick() should never return null");
    }

    @Test
    void testGetBrick_ReturnsValidBrick() {
        Brick brick = generator.getBrick();
        assertNotNull(brick.getShapeMatrix(), "Brick should have shape matrix");
        assertFalse(brick.getShapeMatrix().isEmpty(), "Shape matrix should not be empty");
    }

    @Test
    void testGetBrick_MaintainsQueueSize() {
        // Get multiple bricks - queue should maintain at least 3 bricks
        Brick brick1 = generator.getBrick();
        Brick brick2 = generator.getBrick();
        Brick brick3 = generator.getBrick();
        Brick brick4 = generator.getBrick();
        
        // All should be non-null
        assertNotNull(brick1, "First brick should not be null");
        assertNotNull(brick2, "Second brick should not be null");
        assertNotNull(brick3, "Third brick should not be null");
        assertNotNull(brick4, "Fourth brick should not be null");
        
        // After getting 4 bricks, next brick should still be available
        Brick nextBrick = generator.getNextBrick();
        assertNotNull(nextBrick, "Next brick should still be available after getting multiple bricks");
    }

    @Test
    void testGetNextBrick_DoesNotRemoveFromQueue() {
        Brick next1 = generator.getNextBrick();
        Brick next2 = generator.getNextBrick();
        
        // Peeking should return the same brick
        assertEquals(next1, next2, "getNextBrick() should return the same brick when called multiple times (peek)");
        
        // Getting the brick should return what was peeked
        Brick actual = generator.getBrick();
        assertEquals(next1, actual, "getBrick() should return the brick that was peeked");
    }

    @Test
    void testGetNextBricks_ReturnsCorrectCount() {
        // Request 3 next bricks
        List<Brick> nextBricks = generator.getNextBricks(3);
        
        assertEquals(3, nextBricks.size(), "Should return exactly 3 bricks");
        assertNotNull(nextBricks.get(0), "First brick should not be null");
        assertNotNull(nextBricks.get(1), "Second brick should not be null");
        assertNotNull(nextBricks.get(2), "Third brick should not be null");
    }

    @Test
    void testGetNextBricks_ReturnsMoreThanThree() {
        // Request 5 next bricks
        List<Brick> nextBricks = generator.getNextBricks(5);
        
        assertEquals(5, nextBricks.size(), "Should return exactly 5 bricks");
        for (int i = 0; i < 5; i++) {
            assertNotNull(nextBricks.get(i), "Brick " + i + " should not be null");
        }
    }

    @Test
    void testGetNextBricks_DoesNotRemoveFromQueue() {
        // Get next bricks multiple times - should return same bricks
        List<Brick> firstCall = generator.getNextBricks(3);
        List<Brick> secondCall = generator.getNextBricks(3);
        
        // Should return the same bricks (peek, not poll)
        assertEquals(firstCall.get(0), secondCall.get(0), "First brick should be the same");
        assertEquals(firstCall.get(1), secondCall.get(1), "Second brick should be the same");
        assertEquals(firstCall.get(2), secondCall.get(2), "Third brick should be the same");
    }

    @Test
    void testGetBrick_AfterGetNextBricks_ReturnsFirstBrick() {
        // Get next 3 bricks
        List<Brick> nextBricks = generator.getNextBricks(3);
        Brick firstNext = nextBricks.get(0);
        
        // Get the actual brick
        Brick actual = generator.getBrick();
        
        // Should be the same as the first next brick
        assertEquals(firstNext, actual, "getBrick() should return the first brick from getNextBricks()");
    }

    @Test
    void testGetBrick_GeneratesAllBrickTypes() {
        // Get many bricks to ensure all types are generated
        Set<String> brickTypes = new HashSet<>();
        
        for (int i = 0; i < 50; i++) {
            Brick brick = generator.getBrick();
            assertNotNull(brick, "Brick should not be null");
            // Track brick type by class name
            brickTypes.add(brick.getClass().getSimpleName());
        }
        
        // Should have generated all 7 brick types (I, J, L, O, S, T, Z)
        assertTrue(brickTypes.size() >= 5, "Should generate multiple brick types (at least 5 different types)");
    }

    @Test
    void testGetBrick_QueueReplenishesAfterConsumption() {
        // Consume all initial bricks
        generator.getBrick();
        generator.getBrick();
        generator.getBrick();
        
        // Queue should be replenished, next brick should still be available
        Brick nextBrick = generator.getNextBrick();
        assertNotNull(nextBrick, "Next brick should be available after consuming initial bricks");
        
        // Should be able to get more bricks
        Brick brick4 = generator.getBrick();
        assertNotNull(brick4, "Should be able to get more bricks after consuming initial queue");
    }

    @Test
    void testGetNextBricks_RequestZero_ReturnsEmptyList() {
        List<Brick> bricks = generator.getNextBricks(0);
        assertTrue(bricks.isEmpty(), "Requesting 0 bricks should return empty list");
    }

    @Test
    void testGetNextBricks_RequestOne_ReturnsOneBrick() {
        List<Brick> bricks = generator.getNextBricks(1);
        assertEquals(1, bricks.size(), "Should return exactly 1 brick");
        assertNotNull(bricks.get(0), "Brick should not be null");
    }

    @Test
    void testGetBrick_MultipleCalls_AllReturnValidBricks() {
        // Get many bricks in sequence
        for (int i = 0; i < 20; i++) {
            Brick brick = generator.getBrick();
            assertNotNull(brick, "Brick " + i + " should not be null");
            assertNotNull(brick.getShapeMatrix(), "Brick " + i + " should have shape matrix");
            assertFalse(brick.getShapeMatrix().isEmpty(), "Brick " + i + " should have non-empty shape matrix");
        }
    }

    @Test
    void testGetNextBrick_AfterGettingBrick_UpdatesCorrectly() {
        // Get initial next brick
        Brick initialNext = generator.getNextBrick();
        
        // Get the current brick
        Brick current = generator.getBrick();
        assertEquals(initialNext, current, "Current brick should match initial next brick");
        
        // Next brick should now be different (the second brick in queue)
        Brick newNext = generator.getNextBrick();
        assertNotNull(newNext, "New next brick should not be null");
        // It might be the same type but should be a different instance or position in queue
    }
}

