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
     * All lines added in the same call will have the same hole position.
     * @param numLines Number of garbage lines to add
     */
    public void addGarbage(int numLines) {
        if (numLines <= 0) {
            return;
        }
        
        // Generate one random hole position that will be used for all rows added at the same time
        int holePosition = random.nextInt(boardWidth);
        
        for (int i = 0; i < numLines; i++) {
            int[] garbageLine = generateGarbageLine(holePosition);
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
     * Generates a garbage line with a specified hole position.
     * The line is filled with a garbage block type (using type 8 to distinguish from normal blocks).
     * One position is ALWAYS left empty (0) to create a hole, ensuring the row is never completely solid.
     * @param holePosition The position where the hole should be (0 to boardWidth-1)
     */
    private int[] generateGarbageLine(int holePosition) {
        int[] line = new int[boardWidth];
        // Fill the line with garbage blocks (type 8)
        for (int i = 0; i < boardWidth; i++) {
            line[i] = 8; // Use type 8 for garbage blocks
        }
        
        // ALWAYS create one hole at the specified position - this ensures the rubbish row is never completely solid
        // and can only be cleared when the player fills the empty cell
        line[holePosition] = 0;
        
        return line;
    }
}

