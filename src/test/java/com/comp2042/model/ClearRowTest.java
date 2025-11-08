package com.comp2042.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearRowTest {

    @Test
    void testClearRowCreation() {
        int[][] newMatrix = new int[5][5];
        int linesRemoved = 2;
        int scoreBonus = 200;
        
        ClearRow clearRow = new ClearRow(linesRemoved, newMatrix, scoreBonus);
        
        assertEquals(linesRemoved, clearRow.getLinesRemoved());
        assertEquals(scoreBonus, clearRow.getScoreBonus());
        assertNotNull(clearRow.getNewMatrix());
    }

    @Test
    void testClearRowImmutability() {
        int[][] originalMatrix = new int[5][5];
        originalMatrix[0][0] = 1;
        
        ClearRow clearRow = new ClearRow(1, originalMatrix, 50);
        int[][] returnedMatrix = clearRow.getNewMatrix();
        
        // Modify original
        originalMatrix[0][0] = 99;
        
        // Returned matrix should be a copy
        assertNotEquals(99, returnedMatrix[0][0]);
    }
}

