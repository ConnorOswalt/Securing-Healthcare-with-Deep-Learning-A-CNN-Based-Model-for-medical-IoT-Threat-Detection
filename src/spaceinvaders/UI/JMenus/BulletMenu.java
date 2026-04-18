package spaceinvaders.UI.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class BulletMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Bullet";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Bullet/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().bulletMenuListener();
    }
}