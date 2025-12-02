package com.comp2042.util;

import com.comp2042.bricks.Brick;
import com.comp2042.model.NextShapeInfo;

/**
 * Utility class for managing brick rotation states.
 * Tracks the current rotation state of a brick and provides methods to get
 * the current shape and preview the next rotation state.
 */
public final class BrickRotator {

    private Brick brick;
    private int currentShape = 0;
    
    /**
     * Creates a new BrickRotator instance.
     */
    public BrickRotator() {
    }

    /**
     * Gets the next rotation state of the current brick.
     * 
     * @return NextShapeInfo containing the next shape matrix and rotation index
     */
    public NextShapeInfo getNextShape() {
        int nextShape = (currentShape + 1) % brick.getShapeMatrix().size();
        return new NextShapeInfo(brick.getShapeMatrix().get(nextShape), nextShape);
    }

    /**
     * Gets the current rotation state shape matrix.
     * 
     * @return The 2D array representing the current brick shape in its current rotation
     */
    public int[][] getCurrentShape() {
        return brick.getShapeMatrix().get(currentShape);
    }

    /**
     * Sets the current rotation state index.
     * 
     * @param currentShape The rotation state index (0-based)
     */
    public void setCurrentShape(int currentShape) {
        this.currentShape = currentShape;
    }

    /**
     * Sets the brick and resets the rotation state to the initial state.
     * 
     * @param brick The brick to rotate
     */
    public void setBrick(Brick brick) {
        this.brick = brick;
        currentShape = 0;
    }

    /**
     * Gets the current brick.
     * 
     * @return The current Brick instance
     */
    public Brick getBrick() {
        return brick;
    }
}

