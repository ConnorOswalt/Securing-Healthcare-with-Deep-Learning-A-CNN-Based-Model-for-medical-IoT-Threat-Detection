package spaceinvaders.UI.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class EffectsMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Effects";
    }

    @Override
    protected ArrayList<String> setButtonTitles() {
        return new ArrayList<>(Arrays.asList("Enable Explosions", "Disable Explosions"));
    }

    @Override
    protected ArrayList<String> setButtonPaths() {
        return new ArrayList<>(Arrays.asList("enable_explosions", "disable_explosions"));
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().effectsMenuListener();
    }
}
