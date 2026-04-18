package spaceinvaders.DataHandlers;

import spaceinvaders.GameExceptions;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * MusicHandler runs on its own thread and manages background music playback.
 * Menu actions post track selections via selectTrack, and this thread applies
 * changes off the EDT to keep UI interactions responsive.
 */
public class MusicHandler extends Thread {
    private volatile boolean running = true;
    private String pendingTrackResourcePath;
    private boolean hasPendingTrack = false;
    private Clip clip;
    private boolean muted = false;
    private int volumePercent = 80;

    public MusicHandler() {
        setDaemon(true);
        setName("MusicHandler-Thread");
    }

    @Override
    public void run() {
        while (running) {
            String trackPath;
            synchronized (this) {
                while (running && !hasPendingTrack) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        GameExceptions.handleInterrupted("MusicHandler wait", e);
                        running = false;
                        break;
                    }
                }

                if (!running) {
                    break;
                }

                trackPath = pendingTrackResourcePath;
                hasPendingTrack = false;
            }

            playTrack(trackPath);
        }

        closeClip();
    }

    public synchronized void selectTrack(String resourcePath) {
        pendingTrackResourcePath = resourcePath;
        hasPendingTrack = true;
        notifyAll();
    }

    public synchronized void stopThread() {
        running = false;
        notifyAll();
    }

    public synchronized void stopCurrentTrack() {
        closeClip();
    }

    public void playOneShotEffect(String resourcePath) {
        Thread effectThread = new Thread(() -> playEffectNow(resourcePath), "SoundEffect-Thread");
        effectThread.setDaemon(true);
        effectThread.start();
    }

    public synchronized void setMuted(boolean muted) {
        this.muted = muted;

        if (clip == null) {
            return;
        }

        // Make mute/unmute feel immediate by pausing/resuming playback,
        // not just changing gain on a running buffered clip.
        if (muted) {
            if (clip.isRunning()) {
                clip.stop();
            }
            applyVolumeToClip(clip);
            return;
        }

        applyVolumeToClip(clip);
        if (!clip.isRunning()) {
            clip.start();
        }
    }

    public synchronized boolean isMuted() {
        return muted;
    }

    public synchronized void setVolumePercent(int volumePercent) {
        this.volumePercent = Math.max(0, Math.min(100, volumePercent));
        applyVolumeToCurrentClip();
    }

    public synchronized int getVolumePercent() {
        return volumePercent;
    }

    private void playTrack(String resourcePath) {
        closeClip();

        URL trackUrl = MusicHandler.class.getResource(resourcePath);
        if (trackUrl == null) {
            GameExceptions.showErrorDialog("Music track not found: " + resourcePath);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(trackUrl);
            Clip newClip = AudioSystem.getClip();
            newClip.open(audioStream);
            applyVolumeToClip(newClip);
            newClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (!muted) {
                newClip.start();
            }
            clip = newClip;
        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e) {
            GameExceptions.handleWithDialog("Failed to play music track", e);
        }
    }

    private synchronized void applyVolumeToCurrentClip() {
        if (clip != null) {
            applyVolumeToClip(clip);
        }
    }

    private void applyVolumeToClip(Clip targetClip) {
        if (!targetClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) targetClip.getControl(FloatControl.Type.MASTER_GAIN);
        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();

        if (muted || volumePercent <= 0) {
            gainControl.setValue(min);
            return;
        }

        float normalized = volumePercent / 100.0f;
        float db = (float) (20.0 * Math.log10(normalized));
        if (db < min) {
            db = min;
        }
        if (db > max) {
            db = max;
        }
        gainControl.setValue(db);
    }

    private void closeClip() {
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.close();
            clip = null;
        }
    }

    private void playEffectNow(String resourcePath) {
        URL effectUrl = MusicHandler.class.getResource(resourcePath);
        if (effectUrl == null) {
            GameExceptions.showErrorDialog("Sound effect not found: " + resourcePath);
            return;
        }

        try (AudioInputStream audioStream = AudioSystem.getAudioInputStream(effectUrl)) {
            Clip effectClip = AudioSystem.getClip();
            effectClip.open(audioStream);
            effectClip.start();

            long effectDurationMs = Math.max(100L, effectClip.getMicrosecondLength() / 1000L);
            try {
                Thread.sleep(effectDurationMs);
            } catch (InterruptedException e) {
                GameExceptions.handleInterrupted("Sound effect playback", e);
            }

            effectClip.close();
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            GameExceptions.handleWithDialog("Failed to play sound effect", e);
        }
    }
}
