package com.comp2042.controller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;

/**
 * Manages all audio operations for the game.
 * Follows Single Responsibility Principle - only handles audio playback.
 */
public class AudioManager {
    
    private static final double DEFAULT_VOLUME = 0.5;
    
    private MediaPlayer countdownSound;
    private MediaPlayer gameMusic;
    private MediaPlayer gameOverSound;
    private MediaPlayer lineClearSound;
    private MediaPlayer mainMenuMusic;
    private MediaPlayer winnerSound;
    
    private double currentVolume = DEFAULT_VOLUME;
    private boolean isMuted = false;
    
    /**
     * Initializes all audio players by loading audio files.
     */
    public void initialize() {
        try {
            loadCountdownSound();
            loadGameMusic();
            loadGameOverSound();
            loadLineClearSound();
            loadMainMenuMusic();
            loadWinnerSound();
            applyVolumeToAllPlayers(currentVolume);
        } catch (Exception e) {
            System.err.println("Error loading audio files: " + e.getMessage());
        }
    }
    
    private void loadCountdownSound() {
        URL countdownUrl = getClass().getClassLoader().getResource("audio/3-2-1-countdown.mp3");
        if (countdownUrl != null) {
            Media countdownMedia = new Media(countdownUrl.toExternalForm());
            countdownSound = new MediaPlayer(countdownMedia);
        }
    }
    
    private void loadGameMusic() {
        URL gameMusicUrl = getClass().getClassLoader().getResource("audio/A-Type Music (Korobeiniki).mp3");
        if (gameMusicUrl != null) {
            Media gameMusicMedia = new Media(gameMusicUrl.toExternalForm());
            gameMusic = new MediaPlayer(gameMusicMedia);
            gameMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    private void loadGameOverSound() {
        URL gameOverUrl = getClass().getClassLoader().getResource("audio/Game Over.mp3");
        if (gameOverUrl != null) {
            Media gameOverMedia = new Media(gameOverUrl.toExternalForm());
            gameOverSound = new MediaPlayer(gameOverMedia);
        }
    }
    
    private void loadLineClearSound() {
        URL lineClearUrl = getClass().getClassLoader().getResource("audio/Stage Clear.mp3");
        if (lineClearUrl != null) {
            Media lineClearMedia = new Media(lineClearUrl.toExternalForm());
            lineClearSound = new MediaPlayer(lineClearMedia);
        }
    }
    
    private void loadMainMenuMusic() {
        URL mainMenuUrl = getClass().getClassLoader().getResource("audio/tetris-party-deluxe-main-menu-music.mp3");
        if (mainMenuUrl != null) {
            Media mainMenuMedia = new Media(mainMenuUrl.toExternalForm());
            mainMenuMusic = new MediaPlayer(mainMenuMedia);
            mainMenuMusic.setCycleCount(MediaPlayer.INDEFINITE);
        }
    }
    
    private void loadWinnerSound() {
        URL winnerUrl = getClass().getClassLoader().getResource("audio/winners_W9Cpenj.mp3");
        if (winnerUrl != null) {
            Media winnerMedia = new Media(winnerUrl.toExternalForm());
            winnerSound = new MediaPlayer(winnerMedia);
        }
    }


    
    private void applyVolumeToAllPlayers(double volume) {
        if (countdownSound != null) {
            countdownSound.setVolume(volume);
        }
        if (gameMusic != null) {
            gameMusic.setVolume(volume);
        }
        if (gameOverSound != null) {
            gameOverSound.setVolume(volume);
        }
        if (lineClearSound != null) {
            lineClearSound.setVolume(volume);
        }
        if (mainMenuMusic != null) {
            mainMenuMusic.setVolume(volume);
        }
        if (winnerSound != null) {
            winnerSound.setVolume(volume);
        }
    }

    

}

