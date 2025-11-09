package com.comp2042.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HighScoreManager {
    
    private static final int MAX_HIGH_SCORES = 3;
    private final List<Integer> highScores;
    
    public HighScoreManager() {
        highScores = new ArrayList<>();
    }
    
    public void addScore(int score) {
        if (score > 0) {
            highScores.add(score);
            Collections.sort(highScores, Collections.reverseOrder());
            if (highScores.size() > MAX_HIGH_SCORES) {
                highScores.remove(highScores.size() - 1);
            }
        }
    }
    
    public List<Integer> getTopScores(int count) {
        List<Integer> topScores = new ArrayList<>();
        for (int i = 0; i < Math.min(count, highScores.size()); i++) {
            topScores.add(highScores.get(i));
        }
        return topScores;
    }
    
    public List<Integer> getTop3Scores() {
        return getTopScores(3);
    }
}

