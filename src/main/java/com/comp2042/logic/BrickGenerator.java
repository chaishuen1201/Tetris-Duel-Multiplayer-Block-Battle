package com.comp2042.logic;

import com.comp2042.bricks.Brick;

/**
 * Interface for generating bricks in the game.
 * Provides methods to get the current brick and preview the next brick.
 * Implementations can use different strategies such as random generation
 * or predefined sequences.
 */
public interface BrickGenerator {

    /**
     * Gets the current brick to be placed on the board.
     * 
     * @return The current Brick instance
     */
    Brick getBrick();

    /**
     * Gets the next brick that will be placed after the current one.
     * Used for preview display purposes.
     * 
     * @return The next Brick instance
     */
    Brick getNextBrick();
}
