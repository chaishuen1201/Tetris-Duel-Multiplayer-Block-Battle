package com.comp2042.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Manages game timers for single player and multiplayer modes.
 * Follows Single Responsibility Principle - only handles timer operations.
 */
public class TimerManager {
    
    private Timeline singlePlayerTimer;
    private Timeline multiplayerTimer;
    private int singlePlayerElapsedSeconds = 0;
    private int multiplayerElapsedSeconds = 0;
    private Label singlePlayerTimerLabel;
    private Label multiplayerTimerLabel;
    
    /**
     * Sets the timer label for single player mode.
     * @param label The label to display the timer
     */
    public void setSinglePlayerTimerLabel(Label label) {
        this.singlePlayerTimerLabel = label;
        if (label != null) {
            label.setText(formatTime(0));
        }
    }
    
    /**
     * Sets the timer label for multiplayer mode.
     * @param label The label to display the timer
     */
    public void setMultiplayerTimerLabel(Label label) {
        this.multiplayerTimerLabel = label;
        if (label != null) {
            label.setText(formatTime(0));
        }
    }
    
    /**
     * Starts the single player timer.
     */
    public void startSinglePlayerTimer() {
        stopSinglePlayerTimer();
        singlePlayerElapsedSeconds = 0;
        if (singlePlayerTimerLabel != null) {
            singlePlayerTimerLabel.setText(formatTime(0));
        }
        
        singlePlayerTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            singlePlayerElapsedSeconds++;
            if (singlePlayerTimerLabel != null) {
                singlePlayerTimerLabel.setText(formatTime(singlePlayerElapsedSeconds));
            }
        }));
        singlePlayerTimer.setCycleCount(Timeline.INDEFINITE);
        singlePlayerTimer.play();
    }
    
    /**
     * Stops the single player timer.
     */
    public void stopSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.stop();
            singlePlayerTimer = null;
        }
    }
    
    /**
     * Pauses the single player timer.
     */
    public void pauseSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.pause();
        }
    }
    
    /**
     * Resumes the single player timer.
     */
    public void resumeSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.play();
        }
    }
    
    /**
     * Gets the elapsed time in seconds for single player.
     * @return Elapsed seconds
     */
    public int getSinglePlayerElapsedSeconds() {
        return singlePlayerElapsedSeconds;
    }
    
    /**
     * Starts the multiplayer timer.
     */
    public void startMultiplayerTimer() {
        stopMultiplayerTimer();
        multiplayerElapsedSeconds = 0;
        if (multiplayerTimerLabel != null) {
            multiplayerTimerLabel.setText(formatTime(0));
        }
        
        multiplayerTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            multiplayerElapsedSeconds++;
            if (multiplayerTimerLabel != null) {
                multiplayerTimerLabel.setText(formatTime(multiplayerElapsedSeconds));
            }
        }));
        multiplayerTimer.setCycleCount(Timeline.INDEFINITE);
        multiplayerTimer.play();
    }
    
    /**
     * Stops the multiplayer timer.
     */
    public void stopMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.stop();
            multiplayerTimer = null;
        }
    }
    
    /**
     * Pauses the multiplayer timer.
     */
    public void pauseMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.pause();
        }
    }
    
    /**
     * Resumes the multiplayer timer.
     */
    public void resumeMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.play();
        }
    }
    
    /**
     * Gets the elapsed time in seconds for multiplayer.
     * @return Elapsed seconds
     */
    public int getMultiplayerElapsedSeconds() {
        return multiplayerElapsedSeconds;
    }
    
    /**
     * Formats elapsed seconds as MM:SS.
     * @param seconds Total seconds
     * @return Formatted time string
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    /**
     * Stops all timers and cleans up resources.
     */
    public void dispose() {
        stopSinglePlayerTimer();
        stopMultiplayerTimer();
    }
}

