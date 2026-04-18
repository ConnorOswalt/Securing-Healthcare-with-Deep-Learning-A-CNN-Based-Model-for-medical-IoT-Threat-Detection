package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class InvaderMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Invader";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Invader/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().invaderMenuListener();
    }
}