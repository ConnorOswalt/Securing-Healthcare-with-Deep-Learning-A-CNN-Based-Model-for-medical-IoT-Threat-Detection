package spaceinvaders.DataHandlers.MenuImplementations;

import spaceinvaders.GameExceptions;
import spaceinvaders.UI.SpaceInvadersUI;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class ThemeImplementation {
    private static final Pattern STRING_FIELD_PATTERN_TEMPLATE =
            Pattern.compile("\"%s\"\\s*:\\s*\"([^\"]+)\"");
    private static final Object THEME_LOCK = new Object();
    private static SpaceInvadersUI pendingGame;
    private static String pendingThemePath;
    private static boolean hasPendingTheme;

    static {
        Thread themeThread = new Thread(ThemeImplementation::processThemeChanges, "ThemeHandler-Thread");
        themeThread.setDaemon(true);
        themeThread.start();
    }

    public ThemeImplementation() {
    }

    public void handleThemeSelection(ActionEvent e) {
        if (!(e.getSource() instanceof JMenuItem)) {
            return;
        }

        JMenuItem selectedItem = (JMenuItem) e.getSource();
        String selectedPath = selectedItem.getName();

        SpaceInvadersUI game = SpaceInvadersUI.getActiveInstance();
        if (game == null) {
            return;
        }

        if (selectedPath == null || selectedPath.isBlank()) {
            String customPath = JOptionPane.showInputDialog(
                    null,
                    "Enter theme JSON path (example: /resources/Themes/Retro.json):");

            if (customPath == null || customPath.isBlank()) {
                return;
            }

            selectedPath = normalizeResourcePath(customPath);
        }

        if (ThemeImplementation.class.getResource(selectedPath) == null) {
            GameExceptions.showErrorDialog("Theme file not found: " + selectedPath);
            return;
        }

        queueThemeChange(game, selectedPath);
    }

    private void queueThemeChange(SpaceInvadersUI game, String selectedPath) {
        synchronized (THEME_LOCK) {
            pendingGame = game;
            pendingThemePath = selectedPath;
            hasPendingTheme = true;
            THEME_LOCK.notifyAll();
        }
    }

    private static void processThemeChanges() {
        while (true) {
            SpaceInvadersUI game;
            String themePath;

            synchronized (THEME_LOCK) {
                while (!hasPendingTheme) {
                    try {
                        THEME_LOCK.wait();
                    } catch (InterruptedException e) {
                        GameExceptions.handleInterrupted("Theme handler", e);
                        return;
                    }
                }

                game = pendingGame;
                themePath = pendingThemePath;
                hasPendingTheme = false;
            }

            try {
                applyThemeFromJson(game, themePath);
            } catch (RuntimeException e) {
                GameExceptions.handleWithDialog("Failed to apply theme", e);
            }
        }
    }

    private static void applyThemeFromJson(SpaceInvadersUI game, String jsonResourcePath) {
        String jsonContent = readResourceFile(jsonResourcePath);
        if (jsonContent == null) {
            GameExceptions.showErrorDialog("Unable to read theme JSON: " + jsonResourcePath);
            return;
        }

        applyImagePath(game, jsonContent, "shooter", game.imageSelection::setShooterImageFromResourcePath);
        applyImagePath(game, jsonContent, "invader", game.imageSelection::setInvaderImageFromResourcePath);
        applyImagePath(game, jsonContent, "bullet", game.imageSelection::setBulletImageFromResourcePath);
        applyImagePath(game, jsonContent, "background", game.imageSelection::setBackgroundImageFromResourcePath);

        String musicPath = extractPath(jsonContent, "music");
        if (musicPath != null && game.getMusicHandler() != null) {
            game.getMusicHandler().selectTrack(musicPath);
        }

        SwingUtilities.invokeLater(game::repaint);
    }

    private static void applyImagePath(SpaceInvadersUI game, String jsonContent, String key, ResourceApplier applier) {
        String resourcePath = extractPath(jsonContent, key);
        if (resourcePath == null) {
            return;
        }
        applier.apply(resourcePath);
    }

    private static String extractPath(String jsonContent, String key) {
        String quotedKey = Pattern.quote(key);
        Pattern pattern = Pattern.compile(String.format(STRING_FIELD_PATTERN_TEMPLATE.pattern(), quotedKey));
        Matcher matcher = pattern.matcher(jsonContent);
        if (!matcher.find()) {
            return null;
        }

        String normalized = normalizeResourcePath(matcher.group(1));
        if (ThemeImplementation.class.getResource(normalized) == null) {
            GameExceptions.showErrorDialog("Theme resource missing for key '" + key + "': " + normalized);
            return null;
        }
        return normalized;
    }

    private static String readResourceFile(String resourcePath) {
        try (InputStream input = ThemeImplementation.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                return null;
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            GameExceptions.handleWithDialog("Unable to read theme file " + resourcePath, e);
            return null;
        }
    }

    private static String normalizeResourcePath(String path) {
        String normalized = path.trim().replace('\\', '/');
        if (normalized.startsWith("src/")) {
            normalized = normalized.substring(3);
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized;
    }

    @FunctionalInterface
    private interface ResourceApplier {
        void apply(String resourcePath);
    }
}
