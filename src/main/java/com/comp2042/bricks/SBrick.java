package com.comp2042.bricks;

import com.comp2042.util.MatrixOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the S-shaped brick (tetromino).
 * Represents the S-shaped piece with 2 rotation states (symmetric).
 */
public final class SBrick implements Brick {

    private final List<int[][]> brickMatrix = new ArrayList<>();

    /**
     * Constructs an S-shaped brick and initializes its rotation states.
     */
    public SBrick() {
        brickMatrix.add(new int[][]{
                {0, 0, 0, 0},
                {0, 5, 5, 0},
                {5, 5, 0, 0},
                {0, 0, 0, 0}
        });
        brickMatrix.add(new int[][]{
                {5, 0, 0, 0},
                {5, 5, 0, 0},
                {0, 5, 0, 0},
                {0, 0, 0, 0}
        });
    }

    @Override
    public List<int[][]> getShapeMatrix() {
        return MatrixOperations.deepCopyList(brickMatrix);
    }
}
