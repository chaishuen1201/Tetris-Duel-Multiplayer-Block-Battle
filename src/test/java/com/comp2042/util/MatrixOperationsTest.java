package com.comp2042.util;

import com.comp2042.model.ClearRow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatrixOperationsTest {

    @Test
    void testCopy() {
        int[][] original = {{1, 2}, {3, 4}};
        int[][] copy = MatrixOperations.copy(original);

        assertNotSame(original, copy);
        assertArrayEquals(original, copy);

        // Modify copy should not affect original
        copy[0][0] = 99;
        assertEquals(1, original[0][0]);
    }

    @Test
    void testIntersect_NoConflict() {
        int[][] matrix = new int[5][5];
        int[][] brick = {{1, 1}, {1, 1}};

        assertFalse(MatrixOperations.intersect(matrix, brick, 1, 1));
    }

    @Test
    void testIntersect_WithConflict() {
        int[][] matrix = new int[5][5];
        matrix[2][2] = 1;
        int[][] brick = {{1, 1}, {1, 1}};

        assertTrue(MatrixOperations.intersect(matrix, brick, 1, 1));
    }

    @Test
    void testIntersect_OutOfBounds() {
        int[][] matrix = new int[5][5];
        int[][] brick = {{1, 1}, {1, 1}};

        // Out of bounds on the right
        assertTrue(MatrixOperations.intersect(matrix, brick, 4, 1));

        // Out of bounds on the bottom
        assertTrue(MatrixOperations.intersect(matrix, brick, 1, 4));

        // Out of bounds on the left
        assertTrue(MatrixOperations.intersect(matrix, brick, -1, 1));
    }

    @Test
    void testMerge() {
        int[][] filledFields = new int[5][5];
        int[][] brick = {{1, 1}, {1, 1}};

        int[][] result = MatrixOperations.merge(filledFields, brick, 1, 1);

        assertEquals(1, result[1][1]);
        assertEquals(1, result[1][2]);
        assertEquals(1, result[2][1]);
        assertEquals(1, result[2][2]);
        assertEquals(0, result[0][0]);
    }

    @Test
    void testCheckRemoving_NoRowsToClear() {
        int[][] matrix = new int[5][5];
        matrix[0][0] = 1;
        matrix[0][1] = 0; // Not full row

        ClearRow result = MatrixOperations.checkRemoving(matrix);

        assertEquals(0, result.getLinesRemoved());
        assertEquals(0, result.getScoreBonus());
    }

    @Test
    void testCheckRemoving_OneRowToClear() {
        int[][] matrix = new int[5][5];
        // Fill row 2 completely
        for (int j = 0; j < 5; j++) {
            matrix[2][j] = 1;
        }
        matrix[0][0] = 1; // Some other cell

        ClearRow result = MatrixOperations.checkRemoving(matrix);

        assertEquals(1, result.getLinesRemoved());
        assertEquals(50, result.getScoreBonus());

        // Check that row 2 is now empty
        int[][] newMatrix = result.getNewMatrix();
        assertEquals(0, newMatrix[4][0]); // Row should be shifted down
    }

    @Test
    void testCheckRemoving_MultipleRowsToClear() {
        int[][] matrix = new int[5][5];
        // Fill rows 1 and 3 completely
        for (int j = 0; j < 5; j++) {
            matrix[1][j] = 1;
            matrix[3][j] = 1;
        }

        ClearRow result = MatrixOperations.checkRemoving(matrix);

        assertEquals(2, result.getLinesRemoved());
        assertEquals(200, result.getScoreBonus()); // 50 * 2 * 2
    }

    @Test
    void testDeepCopyList() {
        int[][] arr1 = {{1, 2}, {3, 4}};
        int[][] arr2 = {{5, 6}, {7, 8}};
        java.util.List<int[][]> original = java.util.Arrays.asList(arr1, arr2);

        java.util.List<int[][]> copy = MatrixOperations.deepCopyList(original);

        assertEquals(original.size(), copy.size());
        assertNotSame(original.get(0), copy.get(0));
        assertArrayEquals(original.get(0), copy.get(0));
    }
}
