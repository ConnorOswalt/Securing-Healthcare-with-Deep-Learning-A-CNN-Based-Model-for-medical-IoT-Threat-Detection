package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;
import java.util.Arrays;

public class BulletMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Bullet";
    }

    @Override
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Bullet 1", "Bullet 2", "Bullet 3", "Bullet 4"));
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().bulletMenuListener();
    }
}