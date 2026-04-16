package spaceinvaders.JMenus.MenuImplementations;

import spaceinvaders.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

public class ShooterImplementation {
    public void handleShooterSelection(ActionEvent e) {
        if (!(e.getSource() instanceof JMenuItem)) {
            return;
        }

        JMenuItem selectedItem = (JMenuItem) e.getSource();
        String selectedPath = selectedItem.getName();

        // The custom option intentionally has no path.
        if (selectedPath == null || selectedPath.isBlank()) {
            return;
        }

        SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
        if (game == null) {
            return;
        }

        game.imageSelection.setShooterImageFromResourcePath(selectedPath);
        game.repaint();
    }
}
