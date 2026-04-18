package spaceinvaders;

import javax.swing.JOptionPane;

public class GameExceptions {

    // Displays a user-visible error message in the UI.
    public static void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Logs non-fatal warnings to stderr.
    public static void logWarning(String warningMessage) {
        System.err.println("Warning: " + warningMessage);
    }

    // Centralized handling for interrupted threads.
    public static void handleInterrupted(String context, InterruptedException exception) {
        logWarning(context + " interrupted: " + exception.getMessage());
        Thread.currentThread().interrupt();
    }

    // Centralized handling for recoverable exceptions where UI feedback is useful.
    public static void handleWithDialog(String context, Exception exception) {
        showErrorDialog(context + ": " + exception.getMessage());
    }
}
