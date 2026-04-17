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
     * Saves the current score to the leaderboard with the player's name.
     * Keeps only the top 10 scores and writes to file.
     * 
     * @param playerName the name of the player
     */
    public synchronized void saveScore(String playerName) {
        // Add new score entry
        ScoreEntry newEntry = new ScoreEntry(playerName, currentScore);
        leaderboard.add(newEntry);
        
        // Sort by score descending
        leaderboard.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        
        // Keep only top 10 entries
        if (leaderboard.size() > 10) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, 10));
        }
        
        // Write to file
        fileHandler.saveScores(leaderboard);
    }


    /**
     * Stops the score manager thread.
     */
    public void stopThread() {
        running = false;
    }
}
