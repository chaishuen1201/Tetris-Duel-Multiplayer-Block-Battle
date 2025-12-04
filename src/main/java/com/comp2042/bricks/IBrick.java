package com.comp2042.bricks;

import com.comp2042.util.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the I-shaped brick (tetromino).
 * Represents the straight line piece with 4 rotation states.
 */
public final class IBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    /**
     * Constructs an I-shaped brick and initializes its rotation states.
     */
    public IBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {0, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 0, 0},
                {0, 1, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }

}
