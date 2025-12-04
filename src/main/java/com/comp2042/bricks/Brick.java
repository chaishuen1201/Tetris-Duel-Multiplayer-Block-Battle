package com.comp2042.bricks;

import java.util.List;

/**
 * Interface representing a Tetris brick (tetromino).
 * Defines the shape matrix that contains all rotation states of the brick.
 * Each brick type implements this interface to provide its specific shape configurations.
 */
public interface Brick {

    /**
     * Gets the list of shape matrices representing all rotation states of the brick.
     * Each matrix in the list represents one rotation state (typically 4 states for 90-degree rotations).
     * 
     * @return A list of 2D arrays, each representing a rotation state of the brick
     */
    List<int[][]> getShapeMatrix();
}
