package spaceinvaders.DataHandlers.MenuImplementations;

import spaceinvaders.GameExceptions;
import spaceinvaders.UI.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class MusicImplementation {
    public void handleMusicSelection(ActionEvent e) {
        if (!(e.getSource() instanceof JMenuItem)) {
            return;
        }

        JMenuItem selectedItem = (JMenuItem) e.getSource();
        String selectedPath = selectedItem.getName();

        SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
        if (game == null || game.getMusicHandler() == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isBlank()) {
            String customPath = JOptionPane.showInputDialog(
                    null,
                    "Enter project resource path (example: /resources/Music/theme.wav):");

            if (customPath == null || customPath.isBlank()) {
                return;
            }

            String normalizedPath = normalizeResourcePath(customPath);
            if (MusicImplementation.class.getResource(normalizedPath) == null) {
                GameExceptions.showErrorDialog("Invalid project resource path: " + customPath);
                return;
            }

            applyOrQueueTrack(game, normalizedPath);
            return;
        }

        applyOrQueueTrack(game, selectedPath);
    }

    private void applyOrQueueTrack(SpaceInvadersUI game, String path) {
        if (game.isGameOver()) {
            game.getMusicHandler().queueTrackWithoutPlaying(path);
        } else {
            game.getMusicHandler().selectTrack(path);
        }
    }

    private String normalizeResourcePath(String customPath) {
        String normalized = customPath.trim().replace('\\', '/');
        if (normalized.startsWith("src/")) {
            normalized = normalized.substring(3);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }
}
