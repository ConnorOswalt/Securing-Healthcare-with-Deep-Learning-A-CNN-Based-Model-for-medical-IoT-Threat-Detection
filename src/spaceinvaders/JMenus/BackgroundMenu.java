package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class BackgroundMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Background";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Background/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().backgroundMenuListener();
    }
}
