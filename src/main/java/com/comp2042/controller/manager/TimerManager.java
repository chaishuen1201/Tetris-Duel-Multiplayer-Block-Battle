package com.comp2042.controller.manager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Manages game timers for both single player and multiplayer modes.
 * This class provides separate timer instances for single player and multiplayer games,
 * tracking elapsed time in seconds and updating UI labels with formatted time displays
 * (MM:SS format). It handles timer lifecycle operations including start, stop, pause,
 * resume, and reset. The manager uses JavaFX Timeline to update the timer every second,
 * automatically updating the associated label when set. It follows the Single Responsibility
 * Principle by exclusively handling timer operations and time formatting.
 */
public class TimerManager {

    /**
     * Default constructor. Initializes the TimerManager.
     * Timers are created when start methods are called.
     */
    public TimerManager() {
        // Default constructor - timers initialized when needed
    }
    
    private Timeline singlePlayerTimer;
    private Timeline multiplayerTimer;
    private int singlePlayerElapsedSeconds = 0;
    private int multiplayerElapsedSeconds = 0;
    private Label singlePlayerTimerLabel;
    private Label multiplayerTimerLabel;
    
    /**
     * Sets the timer label for single player mode.
     * Initializes the label to display "00:00" when set.
     * 
     * @param label The Label to display the single player timer
     */
    public void setSinglePlayerTimerLabel(Label label) {
        this.singlePlayerTimerLabel = label;
        if (label != null) {
            label.setText(formatTime(0));
        }
    }
    
    /**
     * Sets the timer label for multiplayer mode.
     * Initializes the label to display "00:00" when set.
     * 
     * @param label The Label to display the multiplayer timer
     */
    public void setMultiplayerTimerLabel(Label label) {
        this.multiplayerTimerLabel = label;
        if (label != null) {
            label.setText(formatTime(0));
        }
    }
    
    /**
     * Starts the single player timer.
     * Stops any existing timer, resets elapsed time to 0, initializes the label,
     * creates a new Timeline that updates every second, and begins counting.
     * The timer will continue running indefinitely until stopped or paused.
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
     * Stops the single player timer and releases the Timeline instance.
     * The elapsed time is preserved and can be retrieved using getSinglePlayerElapsedSeconds().
     */
    public void stopSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.stop();
            singlePlayerTimer = null;
        }
    }
    
    /**
     * Resets the single player timer by stopping it and resetting elapsed time to 0.
     * Updates the timer label to display "00:00".
     */
    public void resetSinglePlayerTimer() {
        stopSinglePlayerTimer();
        singlePlayerElapsedSeconds = 0;
        if (singlePlayerTimerLabel != null) {
            singlePlayerTimerLabel.setText(formatTime(0));
        }
    }
    
    /**
     * Pauses the single player timer without resetting elapsed time.
     * The timer can be resumed later using resumeSinglePlayerTimer().
     */
    public void pauseSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.pause();
        }
    }
    
    /**
     * Resumes the single player timer from where it was paused.
     * The elapsed time continues from the value it had when paused.
     */
    public void resumeSinglePlayerTimer() {
        if (singlePlayerTimer != null) {
            singlePlayerTimer.play();
        }
    }
    
    /**
     * Gets the elapsed time in seconds for single player mode.
     * 
     * @return The number of seconds elapsed since the timer was started or reset
     */
    public int getSinglePlayerElapsedSeconds() {
        return singlePlayerElapsedSeconds;
    }
    
    /**
     * Starts the multiplayer timer.
     * Stops any existing timer, resets elapsed time to 0, initializes the label,
     * creates a new Timeline that updates every second, and begins counting.
     * The timer will continue running indefinitely until stopped or paused.
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
     * Stops the multiplayer timer and releases the Timeline instance.
     * The elapsed time is preserved and can be retrieved using getMultiplayerElapsedSeconds().
     */
    public void stopMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.stop();
            multiplayerTimer = null;
        }
    }
    
    /**
     * Resets the multiplayer timer by stopping it and resetting elapsed time to 0.
     * Updates the timer label to display "00:00".
     */
    public void resetMultiplayerTimer() {
        stopMultiplayerTimer();
        multiplayerElapsedSeconds = 0;
        if (multiplayerTimerLabel != null) {
            multiplayerTimerLabel.setText(formatTime(0));
        }
    }
    
    /**
     * Pauses the multiplayer timer without resetting elapsed time.
     * The timer can be resumed later using resumeMultiplayerTimer().
     */
    public void pauseMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.pause();
        }
    }
    
    /**
     * Resumes the multiplayer timer from where it was paused.
     * The elapsed time continues from the value it had when paused.
     */
    public void resumeMultiplayerTimer() {
        if (multiplayerTimer != null) {
            multiplayerTimer.play();
        }
    }
    
    /**
     * Gets the elapsed time in seconds for multiplayer mode.
     * 
     * @return The number of seconds elapsed since the timer was started or reset
     */
    public int getMultiplayerElapsedSeconds() {
        return multiplayerElapsedSeconds;
    }
    
    /**
     * Formats elapsed seconds as MM:SS (minutes:seconds).
     * Converts total seconds into a two-digit minutes and two-digit seconds format.
     * 
     * @param seconds The total number of seconds to format
     * @return A formatted time string in MM:SS format (e.g., "05:23" for 323 seconds)
     */
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    /**
     * Stops all timers and cleans up resources.
     * Stops both single player and multiplayer timers, releasing Timeline instances.
     * Should be called when the TimerManager is no longer needed to prevent resource leaks.
     */
    public void dispose() {
        stopSinglePlayerTimer();
        stopMultiplayerTimer();
    }
}

