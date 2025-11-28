package com.comp2042.model;

import com.comp2042.util.MatrixOperations;

public final class ViewData {

    private final int[][] brickData;
    private final int xPosition;
    private final int yPosition;
    private final int[][] nextBrickData;

    public ViewData(int[][] brickData, int xPosition, int yPosition, int[][] nextBrickData) {
        // Copy arrays to ensure immutability - external modifications shouldn't affect ViewData
        this.brickData = MatrixOperations.copy(brickData);
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.nextBrickData = MatrixOperations.copy(nextBrickData);
    }

    public int[][] getBrickData() {
        return MatrixOperations.copy(brickData);
    }

    public int getXPosition() {
        return xPosition;
    }

    public int getYPosition() {
        return yPosition;
    }

    public int[][] getNextBrickData() {
        return MatrixOperations.copy(nextBrickData);
    }
}

