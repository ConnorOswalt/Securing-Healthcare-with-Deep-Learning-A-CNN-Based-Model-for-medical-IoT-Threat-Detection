package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;

import java.awt.event.ActionListener;

public class BulletMenu extends ParentMenu {
    @Override
    protected String setTitle() {
        return "Bullet";
    }

    @Override
<<<<<<< HEAD
    protected String setResourceDirectory() {
        return "/resources/Bullet/";
=======
    protected java.util.ArrayList<String> setButtonTitles() {
        return new java.util.ArrayList<>(java.util.Arrays.asList(
                "Triangle", "Circle", "Bullet", "Rocket", "Custom"));
    }

    @Override
    protected java.util.ArrayList<String> setButtonPaths() {
        return new java.util.ArrayList<>(Arrays.asList(
                null, null, "/spaceinvaders/resources/Bullets/Tank_Shell.png"
                , "/spaceinvaders/resources/Bullets/Rocket.png"));
>>>>>>> origin/ScoreSystem
    }

    @Override
    protected ActionListener getMenuListener() {
        return new ListenerActions().bulletMenuListener();
    }
}