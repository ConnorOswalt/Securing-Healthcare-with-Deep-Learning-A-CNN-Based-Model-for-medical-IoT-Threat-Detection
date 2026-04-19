package spaceinvaders.scores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private ScoreManager scoreManager;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    public Timer refreshTimer;
    private JLabel noScoresLabel;
    
    // Static field to track if leaderboard is enabled
    private static boolean leaderboardEnabled = true;

    public LeaderboardPanel(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Get the leaderboard
        List<ScoreEntry> leaderboard = scoreManager.getLeaderboard();

        if (leaderboard.isEmpty()) {
            // Show "No scores yet!" message
            noScoresLabel = new JLabel("No scores yet!", SwingConstants.CENTER);
            noScoresLabel.setFont(new Font("Arial", Font.BOLD, 24));
            noScoresLabel.setForeground(Color.YELLOW);
            add(noScoresLabel, BorderLayout.CENTER);
        } else {
            setupLeaderboardTable(leaderboard);
        }

        // Start a timer to refresh the leaderboard every 500ms
        refreshTimer = new Timer(500, e -> refreshLeaderboard());
        refreshTimer.start();
    }

    private void setupLeaderboardTable(List<ScoreEntry> leaderboard) {
        // Create table model
        String[] columnNames = {"Rank", "Name", "Score"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        // Populate table with leaderboard data
        for (int i = 0; i < leaderboard.size(); i++) {
            ScoreEntry entry = leaderboard.get(i);
            tableModel.addRow(new Object[]{i + 1, entry.getName(), entry.getScore()});
        }

        // Create and configure table
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setBackground(Color.BLACK);
        leaderboardTable.setForeground(Color.WHITE);
        leaderboardTable.setGridColor(Color.DARK_GRAY);
        leaderboardTable.setRowHeight(25);
        leaderboardTable.setFont(new Font("Arial", Font.PLAIN, 14));

        // Configure header
        leaderboardTable.getTableHeader().setBackground(new Color(40, 40, 40));
        leaderboardTable.getTableHeader().setForeground(Color.YELLOW);
        leaderboardTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        // Set column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBackground(Color.BLACK);
        scrollPane.getViewport().setBackground(Color.BLACK);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void refreshLeaderboard() {
        List<ScoreEntry> leaderboard = scoreManager.getLeaderboard();

        if (leaderboard.isEmpty()) {
            // Switch to "No scores yet!" label if leaderboard is empty
            if (tableModel != null) {
                removeAll();
                if (noScoresLabel == null) {
                    noScoresLabel = new JLabel("No scores yet!", SwingConstants.CENTER);
                    noScoresLabel.setFont(new Font("Arial", Font.BOLD, 24));
                    noScoresLabel.setForeground(Color.YELLOW);
                }
                add(noScoresLabel, BorderLayout.CENTER);
                revalidate();
                repaint();
            }
        } else {
            // Update table data
            if (tableModel == null) {
                // Switch from "No scores yet!" to table
                removeAll();
                setupLeaderboardTable(leaderboard);
                revalidate();
                repaint();
            } else {
                // Refresh existing table
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < leaderboard.size(); i++) {
                    ScoreEntry entry = leaderboard.get(i);
                    tableModel.addRow(new Object[]{i + 1, entry.getName(), entry.getScore()});
                }
            }
        }
    }

    /**
     * Opens the leaderboard in a new JDialog centered on screen.
     * The dialog is non-modal, allowing the game to continue running while viewing the leaderboard.
     * Does nothing if the leaderboard is disabled.
     * 
     * @param manager the ScoreManager to display leaderboard from
     */
    public static void showLeaderboard(ScoreManager manager) {
        // Check if leaderboard is enabled
        if (!leaderboardEnabled) {
            return;
        }
        
        JDialog dialog = new JDialog();
        dialog.setTitle("High Scores");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(null); // Center on screen
        dialog.setResizable(true);
        dialog.setModalityType(Dialog.ModalityType.MODELESS); // Non-modal so game can continue

        // Add the leaderboard panel
        LeaderboardPanel panel = new LeaderboardPanel(manager);
        
        // Stop the refresh timer when dialog is closed
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (panel.refreshTimer != null) {
                    panel.refreshTimer.stop();
                }
            }
        });
        
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    /**
     * Sets whether the leaderboard is enabled.
     * 
     * @param enabled true to enable leaderboard, false to disable
     */
    public static void setLeaderboardEnabled(boolean enabled) {
        leaderboardEnabled = enabled;
    }
    
    /**
     * Gets whether the leaderboard is enabled.
     * 
     * @return true if leaderboard is enabled, false if disabled
     */
    public static boolean isLeaderboardEnabled() {
        return leaderboardEnabled;
    }
}
