package com.comp2042.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages high scores for the game.
 * Maintains a list of the top scores, automatically sorting and limiting
 * to the top 3 scores. Provides methods to add new scores and retrieve top scores.
 */
public class HighScoreManager {
    
    private static final int MAX_HIGH_SCORES = 3;
    private final List<Integer> highScores;
    
    /**
     * Creates a new HighScoreManager with an empty high scores list.
     */
    public HighScoreManager() {
        highScores = new ArrayList<>();
    }
    
    /**
     * Adds a new score to the high scores list if it is greater than 0.
     * Automatically sorts the list in descending order and keeps only the top scores.
     * 
     * @param score The score to add
     */
    public void addScore(int score) {
        if (score > 0) {
            highScores.add(score);
            Collections.sort(highScores, Collections.reverseOrder());
            if (highScores.size() > MAX_HIGH_SCORES) {
                highScores.remove(highScores.size() - 1);
            }
        }
    }
    
    /**
     * Gets the top N scores from the high scores list.
     * 
     * @param count The number of top scores to retrieve
     * @return A list containing the top scores (up to the specified count)
     */
    public List<Integer> getTopScores(int count) {
        List<Integer> topScores = new ArrayList<>();
        for (int i = 0; i < Math.min(count, highScores.size()); i++) {
            topScores.add(highScores.get(i));
        }
        return topScores;
    }
    
    /**
     * Gets the top 3 scores from the high scores list.
     * 
     * @return A list containing the top 3 scores
     */
    public List<Integer> getTop3Scores() {
        return getTopScores(3);
    }
}

