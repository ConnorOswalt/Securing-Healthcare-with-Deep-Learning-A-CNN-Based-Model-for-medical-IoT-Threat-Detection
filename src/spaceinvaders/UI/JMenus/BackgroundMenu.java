package spaceinvaders.UI.JMenus;

import spaceinvaders.DataHandlers.MenuImplementations.BackgroundImplementation;
import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

public class BackgroundMenu extends ParentMenu {
    public BackgroundMenu() {
        super();

        JMenuItem starsItem = new JMenuItem("Stars");
        starsItem.setName(BackgroundImplementation.STARS_BACKGROUND_OPTION);
        starsItem.addActionListener(getMenuListener());
        add(starsItem, Math.max(0, getItemCount() - 1));
    }

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
