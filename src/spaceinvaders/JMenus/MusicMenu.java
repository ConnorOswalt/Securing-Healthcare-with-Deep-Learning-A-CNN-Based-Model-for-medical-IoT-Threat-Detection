package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class MusicMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Music";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Music/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().musicMenuListener();
    }
}