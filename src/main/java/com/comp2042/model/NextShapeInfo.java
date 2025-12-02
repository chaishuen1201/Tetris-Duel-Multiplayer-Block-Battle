package com.comp2042.model;

import com.comp2042.util.MatrixOperations;

/**
 * Immutable data class containing information about the next rotation state of a brick.
 * Stores the shape matrix and the rotation position index for preview purposes.
 */
public final class NextShapeInfo {

    private final int[][] shape;
    private final int position;

    /**
     * Creates a new NextShapeInfo instance with the specified shape and position.
     * 
     * @param shape The 2D array representing the next rotation state shape
     * @param position The rotation state index (0-based)
     */
    public NextShapeInfo(final int[][] shape, final int position) {
        this.shape = shape;
        this.position = position;
    }

    /**
     * Gets a copy of the shape matrix for the next rotation state.
     * 
     * @return A copy of the 2D array representing the next rotation state shape
     */
    public int[][] getShape() {
        return MatrixOperations.copy(shape);
    }

    /**
     * Gets the rotation state position index.
     * 
     * @return The rotation state index (0-based)
     */
    public int getPosition() {
        return position;
    }
}

