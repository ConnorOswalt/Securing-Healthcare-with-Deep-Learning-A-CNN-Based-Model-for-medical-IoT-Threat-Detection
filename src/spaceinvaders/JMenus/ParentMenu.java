package spaceinvaders.JMenus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.util.ArrayList;

public class ParentMenu extends JMenu {
    protected ArrayList<String> buttonPaths;
    protected ArrayList<String> buttonTitles;

    public ParentMenu() {
        this.buttonPaths = setButtonPaths();
        this.buttonTitles = setButtonTitles();
        this.setText(setTitle());

        // Add menu items
        for (String title : buttonTitles) {
            JMenuItem item = new JMenuItem(title);
            // No action for now
            this.add(item);
        }
    }

    protected ArrayList<String> setButtonPaths() {
        return new ArrayList<>();
    }

    protected ArrayList<String> setButtonTitles() {
        return new ArrayList<>(java.util.Arrays.asList(
                "button1", "button2", "button3", "button4"));
    }

    protected String setTitle() {
        return "parent";
    }
}