package spaceinvaders.scores;

import spaceinvaders.GameExceptions;

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
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    // Parse the line as "name,score"
                    String[] parts = line.split(",", 2);
                    if (parts.length != 2) {
                        GameExceptions.logWarning("Malformed score line skipped: " + line);
                        continue;
                    }
                    
                    String name = parts[0].trim();
                    int score = Integer.parseInt(parts[1].trim());
                    scores.add(new ScoreEntry(name, score));
                } catch (NumberFormatException e) {
                    GameExceptions.logWarning("Could not parse score in line: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist, return empty list
            GameExceptions.logWarning("Scores file not found: " + SCORES_FILE);
        } catch (IOException e) {
            GameExceptions.handleWithDialog("Error reading scores file", e);
        }
        
        return scores;
    }

    /**
     * Saves a list of scores to the scores.txt file.
     * Each entry is written as "name,score" on a new line.
     * 
     * @param scores the list of ScoreEntry objects to write
     */
    public void saveScores(List<ScoreEntry> scores) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORES_FILE))) {
            for (ScoreEntry entry : scores) {
                writer.write(entry.getName() + "," + entry.getScore());
                writer.newLine();
            }
        } catch (IOException e) {
            GameExceptions.handleWithDialog("Error writing scores file", e);
        }
    }
}
