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
     * Stops the score manager thread.
     */
    public void stopThread() {
        running = false;
    }
}
