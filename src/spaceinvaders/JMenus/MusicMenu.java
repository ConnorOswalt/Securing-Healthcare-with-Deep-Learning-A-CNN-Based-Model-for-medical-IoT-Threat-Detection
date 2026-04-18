package spaceinvaders.JMenus;

import spaceinvaders.ListenerActions;
import spaceinvaders.SpaceInvadersUI;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class MusicMenu extends ParentMenu {
    public MusicMenu() {
        super();

        addSeparator();

        JCheckBoxMenuItem muteItem = new JCheckBoxMenuItem("Mute");
        if (SpaceInvadersUI.getActiveInstance() != null && SpaceInvadersUI.getActiveInstance().getMusicHandler() != null) {
            muteItem.setState(SpaceInvadersUI.getActiveInstance().getMusicHandler().isMuted());
        }
        muteItem.addActionListener(e -> {
            if (SpaceInvadersUI.getActiveInstance() != null && SpaceInvadersUI.getActiveInstance().getMusicHandler() != null) {
                SpaceInvadersUI.getActiveInstance().getMusicHandler().setMuted(muteItem.getState());
            }
        });
        add(muteItem);

        int currentVolume = 80;
        if (SpaceInvadersUI.getActiveInstance() != null && SpaceInvadersUI.getActiveInstance().getMusicHandler() != null) {
            currentVolume = SpaceInvadersUI.getActiveInstance().getMusicHandler().getVolumePercent();
        }

        JSlider volumeSlider = new JSlider(0, 100, currentVolume);
        volumeSlider.setPreferredSize(new Dimension(140, 24));
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setMinorTickSpacing(5);
        volumeSlider.setPaintTicks(true);

        volumeSlider.addChangeListener(e -> {
            if (SpaceInvadersUI.getActiveInstance() != null && SpaceInvadersUI.getActiveInstance().getMusicHandler() != null) {
                SpaceInvadersUI.getActiveInstance().getMusicHandler().setVolumePercent(volumeSlider.getValue());
            }
        });

        JPanel sliderPanel = new JPanel();
        sliderPanel.add(volumeSlider);
        JMenu volumeMenu = new JMenu("Volume");
        volumeMenu.add(sliderPanel);
        add(volumeMenu);
    }

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