package spaceinvaders.JMenus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ParentMenu extends JMenu {
    protected ArrayList<String> buttonPaths;
    protected ArrayList<String> buttonTitles;

    public ParentMenu() {
        this.buttonPaths = setButtonPaths();
        this.buttonTitles = setButtonTitles();
        this.setText(setTitle());

        // Add menu items with listener and paths
        for (int i = 0; i < buttonTitles.size(); i++) {
            JMenuItem item = new JMenuItem(buttonTitles.get(i));

            item.addActionListener(e -> {
                ActionListener menuListener = getMenuListener();
                if (menuListener != null) {
                    menuListener.actionPerformed(e);
                }
            });
            
            // Set path for the first 3 buttons if available
            if (i < buttonPaths.size()) {
                item.setName(buttonPaths.get(i));
            }
            
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

    protected ActionListener getMenuListener() {
        return null;
    }
}