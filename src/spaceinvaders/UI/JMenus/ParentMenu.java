package spaceinvaders.UI.JMenus;

import spaceinvaders.GameExceptions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class ParentMenu extends JMenu {
    protected ArrayList<String> buttonPaths;
    protected ArrayList<String> buttonTitles;

    public ParentMenu() {
        String resourceDir = setResourceDirectory();
        if (resourceDir != null) {
            this.buttonPaths = new ArrayList<>();
            this.buttonTitles = new ArrayList<>();
            loadFromResourceDirectory(resourceDir);
            this.buttonTitles.add("Custom");
        } else {
            this.buttonPaths = setButtonPaths();
            this.buttonTitles = setButtonTitles();
        }

        this.setText(setTitle());
        ActionListener menuListener = getMenuListener();

        for (int i = 0; i < buttonTitles.size(); i++) {
            JMenuItem item = new JMenuItem(buttonTitles.get(i));
            if (menuListener != null) {
                item.addActionListener(menuListener);
            }
            if (i < buttonPaths.size()) {
                item.setName(buttonPaths.get(i));
            }
            this.add(item);
        }
    }

    private void loadFromResourceDirectory(String resourceDir) {
        URL dirUrl = getClass().getResource(resourceDir);
        if (dirUrl == null) return;
        try {
            File dir = new File(dirUrl.toURI());
            File[] files = dir.listFiles((d, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".png") || lower.endsWith(".gif")
                        || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                        || lower.endsWith(".mp3") || lower.endsWith(".wav")
                        || lower.endsWith(".ogg") || lower.endsWith(".mid")
                    || lower.endsWith(".midi") || lower.endsWith(".json");
            });
            if (files == null) return;
            Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (File file : files) {
                String name = file.getName();
                buttonPaths.add(resourceDir + name);
                buttonTitles.add(name.replaceAll("\\.[^.]+$", ""));
            }
        } catch (URISyntaxException | IllegalArgumentException e) {
            GameExceptions.logWarning("Could not load menu resources from " + resourceDir + ": " + e.getMessage());
        }
    }

    protected String setResourceDirectory() {
        return null;
    }

    protected ArrayList<String> setButtonPaths() {
        return new ArrayList<>();
    }

    protected ArrayList<String> setButtonTitles() {
        return new ArrayList<>(Arrays.asList("button1", "button2", "button3", "button4"));
    }

    protected String setTitle() {
        return "parent";
    }

    protected ActionListener getMenuListener() {
        return null;
    }
}