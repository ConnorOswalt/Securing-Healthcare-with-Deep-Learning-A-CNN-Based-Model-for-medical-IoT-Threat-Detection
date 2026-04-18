package spaceinvaders;

import spaceinvaders.scores.LeaderboardPanel;
import spaceinvaders.UI.SpaceInvadersUI;
import javax.swing.JFrame;
import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Space Invaders with Images");
            SpaceInvadersUI game = new SpaceInvadersUI();
            frame.add(game);
            frame.setSize(600, 700);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Get menu bar and add Scores menu
            JMenuBar menuBar = game.createMenuBar();
            JMenu scoresMenu = new JMenu("Scores");
            JMenuItem viewLeaderboardItem = new JMenuItem("View Leaderboard");
            viewLeaderboardItem.addActionListener(e -> LeaderboardPanel.showLeaderboard(game.getScoreManager()));
            scoresMenu.add(viewLeaderboardItem);
            menuBar.add(scoresMenu);
            
            frame.setJMenuBar(menuBar);
            frame.setVisible(true);
        });
    }
}
