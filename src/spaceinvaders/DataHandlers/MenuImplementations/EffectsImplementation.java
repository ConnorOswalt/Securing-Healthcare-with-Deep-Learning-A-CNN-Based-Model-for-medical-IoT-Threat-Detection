package spaceinvaders.JMenus.MenuImplementations;

import spaceinvaders.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import javax.swing.JMenuItem;

public class EffectsImplementation {
    public void handleEffectsSelection(ActionEvent e) {
        if (!(e.getSource() instanceof JMenuItem)) {
            return;
        }

        JMenuItem selectedItem = (JMenuItem) e.getSource();
        String command = selectedItem.getName();

        SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
        if (game == null || command == null) {
            return;
        }

        if ("enable_explosions".equals(command)) {
            game.setExplosionsEnabled(true);
            return;
        }

        if ("disable_explosions".equals(command)) {
            game.setExplosionsEnabled(false);
        }
    }
}
