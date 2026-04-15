package spaceinvaders.JMenus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.util.ArrayList;

public class ParentMenu extends JMenu {

    protected ArrayList<String> buttonPaths;
    protected String menuTitle = "parent";
    protected ArrayList<String> buttonTitles;

    public ParentMenu() {
        //Paths to items in resources
        buttonPaths = new ArrayList<>();
        //names of menu items
        buttonTitles = new ArrayList<>();
        buttonTitles.add("button1");
        buttonTitles.add("button2");
        buttonTitles.add("button3");
        buttonTitles.add("Custom");

        this.setText(menuTitle);

        // Add menu items
        for (String title : buttonTitles) {
            JMenuItem item = new JMenuItem(title);
            // No action for now
            this.add(item);
        }
    }

}