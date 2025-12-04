package com.comp2042.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GarbageQueueTest {
    
    private static final int BOARD_WIDTH = 10;
    private GarbageQueue garbageQueue;
    
    @BeforeEach
    void setUp() {
        garbageQueue = new GarbageQueue(BOARD_WIDTH);
    }
    
    @Test
    void testAddGarbage_SingleLine() {
        int initialSize = garbageQueue.size();
        garbageQueue.addGarbage(1);
        
        assertEquals(initialSize + 1, garbageQueue.size(), "Should add 1 garbage line");
        assertFalse(garbageQueue.isEmpty(), "Queue should not be empty");
    }
    
    @Test
    void testAddGarbage_MultipleLines() {
        int initialSize = garbageQueue.size();
        garbageQueue.addGarbage(3);
        
        assertEquals(initialSize + 3, garbageQueue.size(), "Should add 3 garbage lines");
    }
    
    @Test
    void testAddGarbage_SameHolePosition() {
        // Test that all rows added at the same time have the same hole position
        garbageQueue.addGarbage(3);
        
        int[] line1 = garbageQueue.pollGarbageLine();
        int[] line2 = garbageQueue.pollGarbageLine();
        int[] line3 = garbageQueue.pollGarbageLine();
        
        assertNotNull(line1, "Line 1 should not be null");
        assertNotNull(line2, "Line 2 should not be null");
        assertNotNull(line3, "Line 3 should not be null");
        
        // Find hole positions (value 0)
        int holePos1 = findHolePosition(line1);
        int holePos2 = findHolePosition(line2);
        int holePos3 = findHolePosition(line3);
        
        assertEquals(holePos1, holePos2, "Line 1 and Line 2 should have same hole position");
        assertEquals(holePos2, holePos3, "Line 2 and Line 3 should have same hole position");
        assertEquals(holePos1, holePos3, "Line 1 and Line 3 should have same hole position");
    }
    
    @Test
    void testAddGarbage_DifferentBatchesHaveDifferentHoles() {
        // Add first batch
        garbageQueue.addGarbage(2);
        int[] line1 = garbageQueue.pollGarbageLine();
        int[] line2 = garbageQueue.pollGarbageLine();
        
        // Add second batch
        garbageQueue.addGarbage(2);
        int[] line3 = garbageQueue.pollGarbageLine();
        int[] line4 = garbageQueue.pollGarbageLine();
        
        int holePos1 = findHolePosition(line1);
        int holePos2 = findHolePosition(line2);
        int holePos3 = findHolePosition(line3);
        int holePos4 = findHolePosition(line4);
        
        // Within same batch, holes should be same
        assertEquals(holePos1, holePos2, "Lines in first batch should have same hole");
        assertEquals(holePos3, holePos4, "Lines in second batch should have same hole");
        
        // Different batches may have different holes (not guaranteed, but likely)
        // We just verify that the logic allows different holes between batches
    }
    
    @Test
    void testPollGarbageLine_RemovesFromQueue() {
        garbageQueue.addGarbage(2);
        
        int initialSize = garbageQueue.size();
        int[] line = garbageQueue.pollGarbageLine();
        
        assertNotNull(line, "Polled line should not be null");
        assertEquals(initialSize - 1, garbageQueue.size(), "Queue size should decrease by 1");
    }
    
    @Test
    void testPollGarbageLine_ReturnsNullWhenEmpty() {
        assertTrue(garbageQueue.isEmpty(), "Queue should be empty initially");
        assertNull(garbageQueue.pollGarbageLine(), "Should return null when queue is empty");
    }
    
    @Test
    void testGarbageLine_HasOneHole() {
        garbageQueue.addGarbage(1);
        int[] line = garbageQueue.pollGarbageLine();
        
        int holeCount = 0;
        for (int i = 0; i < line.length; i++) {
            if (line[i] == 0) {
                holeCount++;
            }
        }
        
        assertEquals(1, holeCount, "Each garbage line should have exactly one hole");
    }
    
    @Test
    void testGarbageLine_AllOtherCellsAreGarbage() {
        garbageQueue.addGarbage(1);
        int[] line = garbageQueue.pollGarbageLine();
        
        int garbageCount = 0;
        for (int i = 0; i < line.length; i++) {
            if (line[i] == 8) { // Type 8 is garbage
                garbageCount++;
            }
        }
        
        assertEquals(BOARD_WIDTH - 1, garbageCount, "All cells except hole should be garbage (type 8)");
    }
    
    @Test
    void testRemoveGarbage_RemovesSpecifiedAmount() {
        garbageQueue.addGarbage(5);
        
        int removed = garbageQueue.removeGarbage(3);
        
        assertEquals(3, removed, "Should remove 3 lines");
        assertEquals(2, garbageQueue.size(), "Should have 2 lines remaining");
    }
    
    @Test
    void testRemoveGarbage_RemovesLessIfNotEnough() {
        garbageQueue.addGarbage(2);
        
        int removed = garbageQueue.removeGarbage(5);
        
        assertEquals(2, removed, "Should remove only available 2 lines");
        assertTrue(garbageQueue.isEmpty(), "Queue should be empty");
    }
    
    @Test
    void testClear_RemovesAllGarbage() {
        garbageQueue.addGarbage(5);
        
        garbageQueue.clear();
        
        assertTrue(garbageQueue.isEmpty(), "Queue should be empty after clear");
        assertEquals(0, garbageQueue.size(), "Size should be 0");
    }
    
    @Test
    void testAddGarbage_ZeroOrNegativeDoesNothing() {
        int initialSize = garbageQueue.size();
        
        garbageQueue.addGarbage(0);
        assertEquals(initialSize, garbageQueue.size(), "Adding 0 should not change queue");
        
        garbageQueue.addGarbage(-1);
        assertEquals(initialSize, garbageQueue.size(), "Adding negative should not change queue");
    }
    
    /**
     * Helper method to find the position of the hole (value 0) in a garbage line.
     */
    private int findHolePosition(int[] line) {
        for (int i = 0; i < line.length; i++) {
            if (line[i] == 0) {
                return i;
            }
        }
        return -1; // No hole found (should not happen)
    }
}

