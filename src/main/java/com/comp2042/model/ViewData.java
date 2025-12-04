package com.comp2042.model;

import com.comp2042.util.MatrixOperations;

/**
 * Immutable data class containing view information for rendering the game.
 * Stores the current brick shape and position, as well as the next brick shape.
 * All arrays are copied to ensure immutability and prevent external modifications.
 */
public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;

    /**
     * Creates a new ViewData instance with the specified brick and position information.
     * All arrays are copied to ensure immutability.
     * 
     * @param brickData The 2D array representing the current brick shape
     * @param xPosition The x-coordinate of the brick's position
     * @param yPosition The y-coordinate of the brick's position
     * @param nextBrickData The 2D array representing the next brick shape
     */
    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData) {
        // Copy arrays to ensure immutability - external modifications shouldn't affect ViewData
        this.brickData = MatrixOperations.copy(brickData);
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = MatrixOperations.copy(nextBrickData);
    }

    /**
     * Gets a copy of the current brick shape data.
     * 
     * @return A copy of the 2D array representing the current brick shape
     */
    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    /**
     * Gets the x-coordinate of the brick's position.
     * 
     * @return The x-coordinate
     */
    public int getXPosition() {
        return xPosition;
    }

    /**
     * Gets the y-coordinate of the brick's position.
     * 
     * @return The y-coordinate
     */
    public int getYPosition() {
        return yPosition;
    }

    /**
     * Gets a copy of the next brick shape data.
     * 
     * @return A copy of the 2D array representing the next brick shape
     */
    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }
}

