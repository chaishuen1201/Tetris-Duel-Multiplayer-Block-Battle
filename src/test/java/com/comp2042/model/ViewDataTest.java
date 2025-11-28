package com.comp2042.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViewDataTest {

    @Test
    void testViewDataCreation() {
        int[][] brickData = {{1, 1}, {1, 1}};
        int[][] nextBrickData = {{2, 2}, {2, 2}};
        int xPos = 5;
        int yPos = 10;
        
        ViewData viewData = new ViewData(brickData, xPos, yPos, nextBrickData);
        
        assertEquals(xPos, viewData.getXPosition());
        assertEquals(yPos, viewData.getYPosition());
        assertArrayEquals(brickData, viewData.getBrickData());
        assertArrayEquals(nextBrickData, viewData.getNextBrickData());
    }

    @Test
    void testViewDataImmutability() {
        int[][] brickData = {{1, 1}, {1, 1}};
        int[][] nextBrickData = {{2, 2}, {2, 2}};
        
        ViewData viewData = new ViewData(brickData, 0, 0, nextBrickData);
        
        // Modify original arrays
        brickData[0][0] = 99;
        nextBrickData[0][0] = 99;
        
        // ViewData should return copies, so original modifications shouldn't affect it
        assertNotEquals(99, viewData.getBrickData()[0][0]);
        assertNotEquals(99, viewData.getNextBrickData()[0][0]);
    }
}

