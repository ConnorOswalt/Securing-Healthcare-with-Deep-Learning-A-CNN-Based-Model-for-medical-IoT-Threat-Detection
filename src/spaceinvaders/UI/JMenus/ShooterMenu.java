package spaceinvaders.UI.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class ShooterMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Shooter";
    }

    @Override
    protected String setResourceDirectory() {
        return "/resources/Shooter/";
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().shooterMenuListener();
    }
}