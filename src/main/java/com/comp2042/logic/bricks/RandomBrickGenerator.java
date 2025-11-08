package com.comp2042.logic.bricks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomBrickGenerator implements BrickGenerator {

    private final List<Brick> brickList;

    private final Deque<Brick> nextBricks = new ArrayDeque<>();

    public RandomBrickGenerator() {
        brickList = new ArrayList<>();
        brickList.add(new IBrick());
        brickList.add(new JBrick());
        brickList.add(new LBrick());
        brickList.add(new OBrick());
        brickList.add(new SBrick());
        brickList.add(new TBrick());
        brickList.add(new ZBrick());
        // Initialize with 3 bricks for displaying next 3 bricks
        nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
    }

    @Override
    public Brick getBrick() {
        // Ensure we have at least 3 bricks in the queue (for displaying next 3 bricks)
        while (nextBricks.size() < 3) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
        return nextBricks.poll();
    }

    @Override
    public Brick getNextBrick() {
        return nextBricks.peek();
    }

    /**
     * Gets the next N bricks without removing them from the queue.
     * Ensures there are enough bricks in the queue for peeking.
     * @param count The number of bricks to peek at
     * @return A list of the next N bricks (may contain fewer if queue is small)
     */
    public List<Brick> getNextBricks(int count) {
        // Ensure we have enough bricks in the queue
        while (nextBricks.size() < count) {
            nextBricks.add(brickList.get(ThreadLocalRandom.current().nextInt(brickList.size())));
        }
        
        List<Brick> result = new ArrayList<>();
        int index = 0;
        for (Brick brick : nextBricks) {
            if (index >= count) break;
            result.add(brick);
            index++;
        }
        return result;
    }
}
