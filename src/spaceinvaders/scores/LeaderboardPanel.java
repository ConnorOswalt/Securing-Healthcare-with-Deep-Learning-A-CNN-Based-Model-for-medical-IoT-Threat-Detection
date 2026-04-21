package spaceinvaders.scores;

import spaceinvaders.UI.SpaceInvadersUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LeaderboardPanel extends JPanel {
    private final ScoreManager scoreManager;
    private final SpaceInvadersUI game;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    public Timer refreshTimer;
    private JLabel noScoresLabel;
    private List<DisplayRow> displayedRows = new ArrayList<>();
    
    // Static field to track if leaderboard is enabled
    private static boolean leaderboardEnabled = true;

    private static class DisplayRow {
        private final String name;
        private final int score;
        private final boolean live;

        private DisplayRow(String name, int score, boolean live) {
            this.name = name;
            this.score = score;
            this.live = live;
        }
    }

    public LeaderboardPanel(ScoreManager scoreManager) {
        this(scoreManager, null);
    }

    public LeaderboardPanel(ScoreManager scoreManager, SpaceInvadersUI game) {
        this.scoreManager = scoreManager;
        this.game = game;
        setBackground(Color.BLACK);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<DisplayRow> leaderboard = buildDisplayRows();

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

    private void setupLeaderboardTable(List<DisplayRow> leaderboard) {
        displayedRows = leaderboard;

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
            DisplayRow entry = leaderboard.get(i);
            tableModel.addRow(new Object[]{i + 1, entry.name, entry.score});
        }

        // Create and configure table
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setBackground(Color.BLACK);
        leaderboardTable.setForeground(Color.WHITE);
        leaderboardTable.setGridColor(Color.DARK_GRAY);
        leaderboardTable.setRowHeight(25);
        leaderboardTable.setFont(new Font("Arial", Font.PLAIN, 14));
        leaderboardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row >= 0 && row < displayedRows.size()) {
                    DisplayRow displayRow = displayedRows.get(row);
                    if (displayRow.live) {
                        c.setBackground(new Color(45, 80, 45));
                        c.setForeground(new Color(230, 255, 230));
                    } else {
                        c.setBackground(Color.BLACK);
                        c.setForeground(Color.WHITE);
                    }
                } else {
                    c.setBackground(Color.BLACK);
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

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
        List<DisplayRow> leaderboard = buildDisplayRows();

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
                displayedRows = leaderboard;
                tableModel.setRowCount(0); // Clear existing rows
                for (int i = 0; i < leaderboard.size(); i++) {
                    DisplayRow entry = leaderboard.get(i);
                    tableModel.addRow(new Object[]{i + 1, entry.name, entry.score});
                }
            }
        }
    }

    private List<DisplayRow> buildDisplayRows() {
        List<DisplayRow> rows = new ArrayList<>();
        List<ScoreEntry> leaderboard = scoreManager.getLeaderboard();
        for (ScoreEntry entry : leaderboard) {
            rows.add(new DisplayRow(entry.getName(), entry.getScore(), false));
        }

        if (game != null) {
            String currentPlayer = game.getPlayerNameForLeaderboard();
            if (currentPlayer != null && !currentPlayer.isBlank()) {
                rows.add(new DisplayRow(currentPlayer, scoreManager.getCurrentScore(), true));
            }
        }

        rows.sort(Comparator.comparingInt((DisplayRow row) -> row.score).reversed());
        if (rows.size() > 10) {
            return new ArrayList<>(rows.subList(0, 10));
        }

        return rows;
    }

    /**
     * Opens the leaderboard in a new JDialog centered on screen.
     * The dialog is non-modal, allowing the game to continue running while viewing the leaderboard.
     * Does nothing if the leaderboard is disabled.
     * 
     * @param manager the ScoreManager to display leaderboard from
     */
    public static void showLeaderboard(ScoreManager manager) {
        showLeaderboard(manager, null);
    }

    /**
     * Opens the leaderboard and includes the current player's live score row when game is provided.
     */
    public static void showLeaderboard(ScoreManager manager, SpaceInvadersUI game) {
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
    LeaderboardPanel panel = new LeaderboardPanel(manager, game);
        
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
