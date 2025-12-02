package com.comp2042.model;

import com.comp2042.util.MatrixOperations;

/**
 * Immutable data class containing information about a row clearing operation.
 * Stores the number of lines removed, the new board matrix after clearing,
 * and the score bonus awarded for the clear.
 */
public final class ClearRow {

    private final int linesRemoved;
    private final int[][] newMatrix;
    private final int scoreBonus;

    /**
     * Creates a new ClearRow instance with the specified clearing information.
     * 
     * @param linesRemoved The number of lines that were cleared
     * @param newMatrix The board matrix after clearing the lines
     * @param scoreBonus The score bonus points awarded for clearing the lines
     */
    public ClearRow(int linesRemoved, int[][] newMatrix, int scoreBonus) {
        this.linesRemoved = linesRemoved;
        this.newMatrix = newMatrix;
        this.scoreBonus = scoreBonus;
    }

    /**
     * Gets the number of lines that were removed.
     * 
     * @return The number of lines removed
     */
    public int getLinesRemoved() {
        return linesRemoved;
    }

    /**
     * Gets a copy of the board matrix after clearing the lines.
     * 
     * @return A copy of the new board matrix
     */
    public int[][] getNewMatrix() {
        return MatrixOperations.copy(newMatrix);
    }

    /**
     * Gets the score bonus points awarded for clearing the lines.
     * 
     * @return The score bonus points
     */
    public int getScoreBonus() {
        return scoreBonus;
    }
}

