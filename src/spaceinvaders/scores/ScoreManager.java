package spaceinvaders.scores;

import java.util.*;

public class ScoreManager extends Thread {
    private volatile int currentScore = 0;
    private List<ScoreEntry> leaderboard;
    private ScoreFileHandler fileHandler;
    private volatile boolean running = true;

    public ScoreManager() {
        fileHandler = new ScoreFileHandler();
        leaderboard = fileHandler.loadScores();
        
        // Set as daemon so it doesn't block app shutdown
        setDaemon(true);
        setName("ScoreManager");
    }

    @Override
    public void run() {
        // Thread runs quietly in the background
        while (running) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Adds points to the current score.
     * 
     * @param points the number of points to add
     */
    public synchronized void addPoints(int points) {
        currentScore += points;
    }

    /**
     * Gets the current score.
     * 
     * @return the current score
     */
    public int getCurrentScore() {
        return currentScore;
    }

    /**
     * Resets the current score to 0.
     */
    public synchronized void resetScore() {
        currentScore = 0;
    }

   

    /**
     * Stops the score manager thread.
     */
    public void stopThread() {
        running = false;
    }
}
