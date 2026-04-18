package spaceinvaders.scores;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public class ScoreManager extends Thread {
    private volatile int currentScore = 0;
    private List<ScoreEntry> leaderboard;
    private ScoreFileHandler fileHandler;
    private volatile boolean running = true;
    
    // Inner class to hold score save data
    private static class ScoreSaveTask {
        String playerName;
        int score;
        
        ScoreSaveTask(String playerName, int score) {
            this.playerName = playerName;
            this.score = score;
        }
    }
    
    private final LinkedBlockingQueue<ScoreSaveTask> saveQueue = new LinkedBlockingQueue<>();

    public ScoreManager() {
        fileHandler = new ScoreFileHandler();
        leaderboard = fileHandler.loadScores();
        
        // Set as daemon so it doesn't block app shutdown
        setDaemon(true);
        setName("ScoreManager");
    }

    @Override
    public void run() {
        // Thread processes save operations from the queue
        while (running) {
            try {
                // Wait for a save operation (blocks until one is available or timeout)
                ScoreSaveTask task = saveQueue.poll(100, java.util.concurrent.TimeUnit.MILLISECONDS);
                
                if (task != null) {
                    // Perform the save operation on this thread
                    performSaveScore(task.playerName, task.score);
                }
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
     * Queues a score save operation to be processed by the ScoreManager thread.
     * The actual file I/O happens on the ScoreManager thread, not the caller's thread.
     * The current score is captured at the time this method is called.
     * 
     * @param playerName the name of the player
     */
    public void saveScore(String playerName) {
        // Capture the current score value at the time of the save
        int scoreToSave = currentScore;
        
        // Queue the save operation with the captured score
        try {
            saveQueue.put(new ScoreSaveTask(playerName, scoreToSave));
        } catch (InterruptedException e) {
            System.out.println("Error queueing score save: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Performs the actual score saving operation (runs on ScoreManager thread).
     * This handles file I/O and leaderboard updates.
     * 
     * @param playerName the name of the player
     * @param score the score to save
     */
    private synchronized void performSaveScore(String playerName, int score) {
        // Add new score entry with the provided score
        ScoreEntry newEntry = new ScoreEntry(playerName, score);
        leaderboard.add(newEntry);
        
        // Sort by score descending
        leaderboard.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        
        // Keep only top 10 entries
        if (leaderboard.size() > 10) {
            leaderboard = new ArrayList<>(leaderboard.subList(0, 10));
        }
        
        // Write to file (I/O operation done on this thread)
        fileHandler.saveScores(leaderboard);
    }

    /**
     * Gets a copy of the current leaderboard.
     * 
     * @return a copy of the leaderboard list
     */
    public synchronized List<ScoreEntry> getLeaderboard() {
        return new ArrayList<>(leaderboard);
    }

    /**
     * Stops the score manager thread.
     */
    public void stopThread() {
        running = false;
    }
}
