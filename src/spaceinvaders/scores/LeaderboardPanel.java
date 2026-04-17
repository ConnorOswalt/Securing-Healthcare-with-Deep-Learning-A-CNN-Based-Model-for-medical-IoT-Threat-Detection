package spaceinvaders.scores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private ScoreManager scoreManager;
    private JTable leaderboardTable;

    public LeaderboardPanel(ScoreManager scoreManager) {
        this.scoreManager = scoreManager;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Get the leaderboard
        List<ScoreEntry> leaderboard = scoreManager.getLeaderboard();

        if (leaderboard.isEmpty()) {
            // Show "No scores yet!" message
            JLabel noScoresLabel = new JLabel("No scores yet!", SwingConstants.CENTER);
            noScoresLabel.setFont(new Font("Arial", Font.BOLD, 24));
            noScoresLabel.setForeground(Color.YELLOW);
            add(noScoresLabel, BorderLayout.CENTER);
        } else {
            // Create table model
            String[] columnNames = {"Rank", "Name", "Score"};
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
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
    }

    
}
