package spaceinvaders;

import javax.swing.JOptionPane;

public class GameExceptions {
    
    // Method to display error dialog
    public static void showErrorDialog(String errorMessage) {
        JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
}