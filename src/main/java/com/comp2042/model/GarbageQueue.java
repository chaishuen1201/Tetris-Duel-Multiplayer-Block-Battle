package com.comp2042.model;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * Manages a queue of pending garbage lines that will be added to the playfield.
 * Garbage lines are stored as arrays representing rows with holes.
 */
public class GarbageQueue {
    private final Deque<int[]> queue;
    private final int boardWidth; // Number of columns (width of the board)
    private final Random random;
    
    public GarbageQueue(int boardWidth) {
        this.queue = new ArrayDeque<>();
        this.boardWidth = boardWidth; // This should be the number of columns (height in matrix terms)
        this.random = new Random();
    }
    
    /**
     * Adds garbage lines to the queue.
     * @param numLines Number of garbage lines to add
     */
    public void addGarbage(int numLines) {
        for (int i = 0; i < numLines; i++) {
            int[] garbageLine = generateGarbageLine();
            queue.addLast(garbageLine);
        }
    }
    
    /**
     * Removes and returns the next garbage line from the queue.
     * @return The next garbage line, or null if queue is empty
     */
    public int[] pollGarbageLine() {
        return queue.pollFirst();
    }
    
    /**
     * Returns the number of pending garbage lines.
     */
    public int size() {
        return queue.size();
    }
    
    /**
     * Checks if the queue is empty.
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }
    
    /**
     * Removes a specified number of garbage lines from the queue (for countering).
     * @param numLines Number of lines to remove
     * @return Number of lines actually removed (may be less if queue had fewer lines)
     */
    public int removeGarbage(int numLines) {
        int removed = 0;
        while (removed < numLines && !queue.isEmpty()) {
            queue.pollFirst();
            removed++;
        }
        return removed;
    }
    
    /**
     * Clears all pending garbage.
     */
    public void clear() {
        queue.clear();
    }
    
    /**
     * Generates a garbage line with a random hole.
     * The line is filled with a garbage block type (using type 8 to distinguish from normal blocks).
     * One random position is ALWAYS left empty (0) to create a hole, ensuring the row is never completely solid.
     */
    private int[] generateGarbageLine() {
        int[] line = new int[boardWidth];
        // Fill the line with garbage blocks (type 8)
        for (int i = 0; i < boardWidth; i++) {
            line[i] = 8; // Use type 8 for garbage blocks
        }
        
        // ALWAYS create one random hole - this ensures the rubbish row is never completely solid
        // and can only be cleared when the player fills the empty cell
        int holePosition = random.nextInt(boardWidth);
        line[holePosition] = 0;
        
        return line;
    }
}

