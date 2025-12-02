package com.comp2042.util;

import com.comp2042.model.ClearRow;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class providing static methods for matrix operations used in the game.
 * This class contains all matrix manipulation operations needed for Tetris gameplay,
 * including collision detection (intersect), matrix copying, merging bricks into the
 * board, checking and clearing completed rows, and deep copying lists of matrices.
 * All methods are static utility functions that operate on 2D integer arrays representing
 * the game board and brick shapes. The class cannot be instantiated and follows the
 * utility class pattern. Matrix operations handle the game board structure where
 * matrix[height][width] represents rows and columns, with 0 representing empty cells
 * and non-zero values representing filled cells of different types.
 */
public final class MatrixOperations {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * 
     * @throws AssertionError Always thrown if instantiation is attempted
     */
    private MatrixOperations() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Checks if a brick shape intersects with the game board matrix at the specified position.
     * Determines if placing the brick at position (x, y) would cause a collision with
     * existing blocks or go out of bounds. Returns true if there's a collision or out-of-bounds
     * condition, false if the brick can be placed at that position.
     * 
     * @param matrix The game board matrix [height][width] representing the current board state
     * @param brick The brick shape matrix to check for intersection
     * @param x The x-coordinate (column) where the brick would be placed
     * @param y The y-coordinate (row) where the brick would be placed
     * @return true if the brick would intersect with existing blocks or go out of bounds,
     *         false if the brick can be placed at the specified position
     */
    public static boolean intersect(final int[][] matrix, final int[][] brick, int x, int y) {
        for (int i = 0; i < brick.length; i++) {
            for (int j = 0; j < brick[i].length; j++) {
                int targetX = x + j;
                int targetY = y + i;
                if (brick[i][j] != 0 && (checkOutOfBound(matrix, targetX, targetY) || matrix[targetY][targetX] != 0)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the specified coordinates are out of bounds for the given matrix.
     * First checks if targetY is within the matrix height bounds, then checks if
     * targetX is within the width bounds of that row.
     * 
     * @param matrix The matrix to check bounds against
     * @param targetX The x-coordinate (column) to check
     * @param targetY The y-coordinate (row) to check
     * @return true if the coordinates are out of bounds, false if they are valid
     */
    private static boolean checkOutOfBound(int[][] matrix, int targetX, int targetY) {
        // Check bounds: targetY must be >= 0 and < matrix.length, then check targetX
        if (targetY < 0 || targetY >= matrix.length) {
            return true; // Out of bounds
        }
        // Now safe to access matrix[targetY]
        return !(targetX >= 0 && targetX < matrix[targetY].length);
    }

    /**
     * Creates a deep copy of a 2D integer array matrix.
     * Allocates new memory for both the outer array and all inner row arrays,
     * ensuring that modifications to the copy do not affect the original.
     * 
     * @param original The matrix to copy
     * @return A new 2D integer array that is a deep copy of the original matrix
     */
    public static int[][] copy(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            int[] row = original[i];
            int rowLength = row.length;
            copy[i] = new int[rowLength];
            System.arraycopy(row, 0, copy[i], 0, rowLength);
        }
        return copy;
    }

    /**
     * Merges a brick shape into the game board matrix at the specified position.
     * Creates a copy of the board and places the brick's non-zero cells into the
     * board at position (x, y). Non-zero cells in the brick overwrite the corresponding
     * cells in the board. Returns a new matrix without modifying the original.
     * 
     * @param filledFields The game board matrix [height][width] to merge the brick into
     * @param brick The brick shape matrix to merge
     * @param x The x-coordinate (column) where the brick should be placed
     * @param y The y-coordinate (row) where the brick should be placed
     * @return A new matrix with the brick merged into the board at the specified position
     */
    public static int[][] merge(int[][] filledFields, int[][] brick, int x, int y) {
        int[][] result = copy(filledFields);
        for (int i = 0; i < brick.length; i++) {
            for (int j = 0; j < brick[i].length; j++) {
                int targetX = x + j;
                int targetY = y + i;
                if (brick[i][j] != 0) {
                    result[targetY][targetX] = brick[i][j];
                }
            }
        }
        return result;
    }

    /**
     * Checks for completed rows and removes them from the matrix.
     * Identifies all rows that are completely filled (no zero cells), removes them,
     * shifts remaining rows down to fill the gaps, and calculates the base score
     * bonus based on the number of lines cleared using standard Tetris scoring:
     * 1 line = 40 points, 2 lines = 100 points, 3 lines = 300 points, 4 lines (Tetris) = 1200 points.
     * Returns a ClearRow object containing the number of lines removed, the new matrix
     * with rows shifted down, and the base score bonus (to be multiplied by level).
     * 
     * @param matrix The game board matrix [height][width] to check for completed rows
     * @return ClearRow containing the number of lines removed, the new matrix with rows
     *         shifted down, and the base score bonus
     */
    public static ClearRow checkRemoving(final int[][] matrix) {
        int[][] result = new int[matrix.length][matrix[0].length];
        Deque<int[]> newRows = new ArrayDeque<>();
        List<Integer> clearedRows = new ArrayList<>();

        for (int i = 0; i < matrix.length; i++) {
            int[] tmpRow = new int[matrix[i].length];
            boolean rowToClear = true;
            for (int j = 0; j < matrix[0].length; j++) {
                if (matrix[i][j] == 0) {
                    rowToClear = false;
                }
                tmpRow[j] = matrix[i][j];
            }
            if (rowToClear) {
                clearedRows.add(i);
            } else {
                newRows.add(tmpRow);
            }
        }
        // Fill result matrix from bottom to top with remaining rows
        // This makes rows fall down when rows above are cleared
        int resultIndex = matrix.length - 1;
        while (!newRows.isEmpty() && resultIndex >= 0) {
            int[] row = newRows.pollLast();
            result[resultIndex] = row;
            resultIndex--;
        }
        // Remaining rows at the top are already initialized to zeros (empty rows)
        // Calculate base score based on Tetris scoring system (without level multiplier)
        // Single: 40, Double: 100, Triple: 300, Tetris: 1200
        int baseScore = 0;
        int linesCleared = clearedRows.size();
        switch (linesCleared) {
            case 1:
                baseScore = 40;
                break;
            case 2:
                baseScore = 100;
                break;
            case 3:
                baseScore = 300;
                break;
            case 4:
                baseScore = 1200;
                break;
            default:
                // For 0 or more than 4 lines (shouldn't happen, but handle gracefully)
                baseScore = 0;
                break;
        }
        return new ClearRow(clearedRows.size(), result, baseScore);
    }

    /**
     * Creates a deep copy of a list of 2D integer arrays.
     * Each matrix in the list is deep copied using the copy() method, ensuring
     * that modifications to matrices in the returned list do not affect the originals.
     * 
     * @param list The list of matrices to deep copy
     * @return A new list containing deep copies of all matrices from the original list
     */
    public static List<int[][]> deepCopyList(List<int[][]> list) {
        return list.stream().map(MatrixOperations::copy).collect(Collectors.toList());
    }
}

