package spaceinvaders.UI.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class ThemesMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Themes";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Themes/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().themesMenuListener();
    }
}
