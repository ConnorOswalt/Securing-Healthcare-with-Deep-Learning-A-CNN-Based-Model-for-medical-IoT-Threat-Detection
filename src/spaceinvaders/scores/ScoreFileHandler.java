package spaceinvaders.scores;

import java.io.*;
import java.util.*;

public class ScoreFileHandler {
    private static final String SCORES_FILE = "scores.txt";

    /**
     * Loads scores from the scores.txt file.
     * Each line should be formatted as "name,score".
     * Malformed lines are skipped with a warning.
     * 
     * @return a List of ScoreEntry objects, or an empty list if the file doesn't exist
     */
    public List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        File file = new File(SCORES_FILE);
        
        // Return empty list if file doesn't exist
        if (!file.exists()) {
            return scores;
        }
        
        
    }
}
