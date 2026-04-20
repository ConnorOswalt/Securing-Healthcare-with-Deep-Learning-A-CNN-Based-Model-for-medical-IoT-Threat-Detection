package spaceinvaders.DataHandlers;

import spaceinvaders.GameExceptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * MusicHandler runs on its own thread and manages background music playback.
 * Menu actions post track selections via selectTrack, and this thread applies
 * changes off the EDT to keep UI interactions responsive.
 */
public class MusicHandler extends Thread {
    private volatile boolean running = true;
    private final ExecutorService effectExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "SoundEffect-Thread");
        t.setDaemon(true);
        return t;
    });
    private String pendingTrackResourcePath;
    private boolean hasPendingTrack = false;
    private volatile Clip loopingEffectClip;
    private volatile boolean loopingEffectStopped = false;
    private boolean pendingRandomStart = false;
    private long pendingMinimumRemainingMs = 0;
    private boolean pendingExplicitStartPosition = false;
    private long pendingStartPositionUs = 0;
    private boolean pendingUseSavedPosition = false;
    private Clip clip;
    private Sequencer midiSequencer;
    private Synthesizer midiSynthesizer;
    private String currentTrackResourcePath;
    private String midiSoundfontResourcePath;
    private String interruptedTrackResourcePath;
    private long interruptedTrackPositionUs = 0;
    private final Map<String, Long> trackResumePositionsUs = new HashMap<>();
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
            boolean randomStart;
            long minimumRemainingMs;
            boolean explicitStartPosition;
            long startPositionUs;
            boolean useSavedPosition;
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
                randomStart = pendingRandomStart;
                minimumRemainingMs = pendingMinimumRemainingMs;
                explicitStartPosition = pendingExplicitStartPosition;
                startPositionUs = pendingStartPositionUs;
                useSavedPosition = pendingUseSavedPosition;
                hasPendingTrack = false;
                pendingRandomStart = false;
                pendingMinimumRemainingMs = 0;
                pendingExplicitStartPosition = false;
                pendingStartPositionUs = 0;
                pendingUseSavedPosition = false;
            }

            playTrack(trackPath, randomStart, minimumRemainingMs, explicitStartPosition, startPositionUs,
                    useSavedPosition);
        }

        closeCurrentTrack();
    }

    public synchronized void selectTrack(String resourcePath) {
        pendingTrackResourcePath = resourcePath;
        pendingRandomStart = false;
        pendingMinimumRemainingMs = 0;
        pendingExplicitStartPosition = false;
        pendingStartPositionUs = 0;
        pendingUseSavedPosition = true;
        hasPendingTrack = true;
        notifyAll();
    }

    public synchronized void selectTrackFromRandomPosition(String resourcePath, long minimumRemainingMs) {
        pendingTrackResourcePath = resourcePath;
        pendingRandomStart = true;
        pendingMinimumRemainingMs = Math.max(0, minimumRemainingMs);
        pendingExplicitStartPosition = false;
        pendingStartPositionUs = 0;
        pendingUseSavedPosition = false;
        hasPendingTrack = true;
        notifyAll();
    }

    public synchronized void startTemporaryOverrideFromRandomPosition(String resourcePath, long minimumRemainingMs) {
        saveInterruptedTrackState();
        selectTrackFromRandomPosition(resourcePath, minimumRemainingMs);
    }

    public synchronized boolean resumeInterruptedTrack() {
        if (interruptedTrackResourcePath == null || interruptedTrackResourcePath.isBlank()) {
            return false;
        }

        pendingTrackResourcePath = interruptedTrackResourcePath;
        pendingRandomStart = false;
        pendingMinimumRemainingMs = 0;
        pendingExplicitStartPosition = true;
        pendingStartPositionUs = interruptedTrackPositionUs;
        pendingUseSavedPosition = false;
        interruptedTrackResourcePath = null;
        interruptedTrackPositionUs = 0;
        hasPendingTrack = true;
        notifyAll();
        return true;
    }

    public synchronized void clearInterruptedTrack() {
        interruptedTrackResourcePath = null;
        interruptedTrackPositionUs = 0;
    }

    public synchronized boolean isTrackActive(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return false;
        }
        return currentTrackResourcePath != null
                && currentTrackResourcePath.equals(resourcePath)
                && (clip != null || midiSequencer != null);
    }

    public synchronized void stopThread() {
        running = false;
        effectExecutor.shutdownNow();
        notifyAll();
    }

    public synchronized void stopCurrentTrack() {
        saveCurrentTrackPosition();
        closeCurrentTrack();
    }

    public synchronized void resumeTrack() {
        if (pendingTrackResourcePath != null) {
            selectTrack(pendingTrackResourcePath);
        }
    }

    /** Stores the track path for later playback without starting it immediately. */
    public synchronized void queueTrackWithoutPlaying(String resourcePath) {
        pendingTrackResourcePath = resourcePath;
        hasPendingTrack = false; // Don't wake the playback thread
    }

    public void playOneShotEffect(String resourcePath) {
        effectExecutor.submit(() -> playEffectNow(resourcePath));
    }

    public void playLoopingEffect(String resourcePath) {
        stopLoopingEffect();
        Thread effectThread = new Thread(() -> startLoopingEffectNow(resourcePath), "LoopingEffect-Thread");
        effectThread.setDaemon(true);
        effectThread.start();
    }

    public void playLoopingEffectSyncedTo(String resourcePath, long periodMs) {
        stopLoopingEffect();
        if (periodMs <= 0) {
            playLoopingEffect(resourcePath);
            return;
        }
        loopingEffectStopped = false;
        Thread t = new Thread(() -> syncedLoopNow(resourcePath, periodMs), "SyncedLoopEffect-Thread");
        t.setDaemon(true);
        t.start();
    }

    public void stopLoopingEffect() {
        loopingEffectStopped = true;
        Clip c = loopingEffectClip;
        if (c != null) {
            c.stop();
            c.close();
            loopingEffectClip = null;
        }
    }

    public synchronized void setMuted(boolean muted) {
        this.muted = muted;

        if (clip == null) {
            if (midiSequencer != null) {
                if (muted) {
                    if (midiSequencer.isRunning()) {
                        midiSequencer.stop();
                    }
                } else if (!midiSequencer.isRunning()) {
                    midiSequencer.start();
                }
                applyVolumeToCurrentTrack();
            }
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
        applyVolumeToCurrentTrack();
    }

    public synchronized int getVolumePercent() {
        return volumePercent;
    }

    public synchronized void setMidiSoundfontResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            midiSoundfontResourcePath = null;
            return;
        }

        midiSoundfontResourcePath = resourcePath;
    }

    private void playTrack(String resourcePath, boolean randomStart, long minimumRemainingMs,
            boolean explicitStartPosition, long startPositionUs, boolean useSavedPosition) {
        saveCurrentTrackPosition();
        closeCurrentTrack();

        if (isMidiResource(resourcePath)) {
            playMidiTrack(resourcePath, randomStart, minimumRemainingMs, explicitStartPosition, startPositionUs,
                    useSavedPosition);
            return;
        }

        URL trackUrl = MusicHandler.class.getResource(resourcePath);
        if (trackUrl == null) {
            GameExceptions.showErrorDialog("Music track not found: " + resourcePath);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(trackUrl);
            Clip newClip = AudioSystem.getClip();
            newClip.open(audioStream);
            if (explicitStartPosition) {
                applyExplicitStartPosition(newClip, startPositionUs);
            } else if (randomStart) {
                applyRandomStartPosition(newClip, minimumRemainingMs);
            } else if (useSavedPosition) {
                applySavedStartPosition(resourcePath, newClip);
            }
            applyVolumeToClip(newClip);
            newClip.loop(Clip.LOOP_CONTINUOUSLY);
            if (!muted) {
                newClip.start();
            }
            clip = newClip;
            currentTrackResourcePath = resourcePath;
        } catch (UnsupportedAudioFileException | LineUnavailableException | java.io.IOException e) {
            GameExceptions.handleWithDialog("Failed to play music track", e);
        }
    }

    private void playMidiTrack(String resourcePath, boolean randomStart, long minimumRemainingMs,
            boolean explicitStartPosition, long startPositionUs, boolean useSavedPosition) {
        URL trackUrl = MusicHandler.class.getResource(resourcePath);
        if (trackUrl == null) {
            GameExceptions.showErrorDialog("MIDI track not found: " + resourcePath);
            return;
        }

        try {
            Sequencer sequencer = MidiSystem.getSequencer(false);
            if (sequencer == null) {
                GameExceptions.showErrorDialog("No MIDI sequencer available on this system.");
                return;
            }

            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            sequencer.open();
            sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());

            // Optional custom soundfont for retro/chiptune instruments.
            // If loading fails, playback continues using the default synth soundbank.
            tryLoadMidiSoundfont(synthesizer);

            sequencer.setSequence(trackUrl.openStream());

            if (explicitStartPosition) {
                applyExplicitStartPosition(sequencer, startPositionUs);
            } else if (randomStart) {
                applyRandomStartPosition(sequencer, minimumRemainingMs);
            } else if (useSavedPosition) {
                applySavedStartPosition(resourcePath, sequencer);
            }

            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            midiSequencer = sequencer;
            midiSynthesizer = synthesizer;
            currentTrackResourcePath = resourcePath;
            applyVolumeToCurrentTrack();
            if (!muted) {
                sequencer.start();
            }
        } catch (MidiUnavailableException | InvalidMidiDataException | IOException e) {
            GameExceptions.handleWithDialog("Failed to play MIDI track", e);
            closeMidiTrack();
        }
    }

    private void applyExplicitStartPosition(Clip targetClip, long startPositionUs) {
        long boundedStart = Math.max(0, Math.min(startPositionUs, targetClip.getMicrosecondLength()));
        targetClip.setMicrosecondPosition(boundedStart);
    }

    private void applyExplicitStartPosition(Sequencer sequencer, long startPositionUs) {
        long boundedStart = Math.max(0, Math.min(startPositionUs, sequencer.getMicrosecondLength()));
        sequencer.setMicrosecondPosition(boundedStart);
    }

    private synchronized void applySavedStartPosition(String resourcePath, Clip targetClip) {
        Long savedPositionUs = trackResumePositionsUs.get(resourcePath);
        if (savedPositionUs == null) {
            return;
        }

        applyExplicitStartPosition(targetClip, savedPositionUs);
    }

    private synchronized void applySavedStartPosition(String resourcePath, Sequencer sequencer) {
        Long savedPositionUs = trackResumePositionsUs.get(resourcePath);
        if (savedPositionUs == null) {
            return;
        }

        applyExplicitStartPosition(sequencer, savedPositionUs);
    }

    private void applyRandomStartPosition(Clip targetClip, long minimumRemainingMs) {
        long clipLengthUs = targetClip.getMicrosecondLength();
        long minimumRemainingUs = minimumRemainingMs * 1000L;
        long maxStartUs = clipLengthUs - minimumRemainingUs;
        if (maxStartUs <= 0) {
            targetClip.setMicrosecondPosition(0);
            return;
        }

        long randomStartUs = (long) (Math.random() * maxStartUs);
        targetClip.setMicrosecondPosition(randomStartUs);
    }

    private void applyRandomStartPosition(Sequencer sequencer, long minimumRemainingMs) {
        long trackLengthUs = sequencer.getMicrosecondLength();
        long minimumRemainingUs = minimumRemainingMs * 1000L;
        long maxStartUs = trackLengthUs - minimumRemainingUs;
        if (maxStartUs <= 0) {
            sequencer.setMicrosecondPosition(0);
            return;
        }

        long randomStartUs = (long) (Math.random() * maxStartUs);
        sequencer.setMicrosecondPosition(randomStartUs);
    }

    private synchronized void applyVolumeToCurrentTrack() {
        if (clip != null) {
            applyVolumeToClip(clip);
        }
        if (midiSynthesizer != null && midiSynthesizer.isOpen()) {
            applyVolumeToMidiSynth(midiSynthesizer);
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

    private void closeCurrentTrack() {
        closeClip();
        closeMidiTrack();
        currentTrackResourcePath = null;
    }

    private void tryLoadMidiSoundfont(Synthesizer synthesizer) {
        String soundfontPath;
        synchronized (this) {
            soundfontPath = midiSoundfontResourcePath;
        }

        if (soundfontPath == null || soundfontPath.isBlank()) {
            return;
        }

        URL soundfontUrl = MusicHandler.class.getResource(soundfontPath);
        if (soundfontUrl == null) {
            return;
        }

        try (InputStream inputStream = soundfontUrl.openStream()) {
            Soundbank soundbank = MidiSystem.getSoundbank(inputStream);
            if (soundbank == null) {
                return;
            }

            Soundbank defaultSoundbank = synthesizer.getDefaultSoundbank();
            if (defaultSoundbank != null) {
                synthesizer.unloadAllInstruments(defaultSoundbank);
            }

            synthesizer.loadAllInstruments(soundbank);
        } catch (InvalidMidiDataException | IOException e) {
            // Silent fallback: keep default synthesizer sounds.
        }
    }

    private void closeMidiTrack() {
        if (midiSequencer != null) {
            if (midiSequencer.isRunning()) {
                midiSequencer.stop();
            }
            midiSequencer.close();
            midiSequencer = null;
        }

        if (midiSynthesizer != null) {
            midiSynthesizer.close();
            midiSynthesizer = null;
        }
    }

    private void applyVolumeToMidiSynth(Synthesizer synthesizer) {
        int channelVolume = muted ? 0 : Math.max(0, Math.min(127, Math.round((volumePercent / 100.0f) * 127)));
        MidiChannel[] channels = synthesizer.getChannels();
        if (channels == null) {
            return;
        }

        for (MidiChannel channel : channels) {
            if (channel != null) {
                channel.controlChange(7, channelVolume); // CC 7 = Channel Volume
            }
        }
    }

    private synchronized void saveCurrentTrackPosition() {
        if (currentTrackResourcePath == null || currentTrackResourcePath.isBlank()) {
            return;
        }

        if (clip != null) {
            trackResumePositionsUs.put(currentTrackResourcePath, clip.getMicrosecondPosition());
            return;
        }

        if (midiSequencer != null) {
            trackResumePositionsUs.put(currentTrackResourcePath, midiSequencer.getMicrosecondPosition());
        }
    }

    private void saveInterruptedTrackState() {
        if (currentTrackResourcePath != null && !currentTrackResourcePath.isBlank()) {
            interruptedTrackResourcePath = currentTrackResourcePath;
            if (clip != null) {
                interruptedTrackPositionUs = clip.getMicrosecondPosition();
                return;
            }
            if (midiSequencer != null) {
                interruptedTrackPositionUs = midiSequencer.getMicrosecondPosition();
                return;
            }
        }

        if (pendingTrackResourcePath != null && !pendingTrackResourcePath.isBlank()) {
            interruptedTrackResourcePath = pendingTrackResourcePath;
            interruptedTrackPositionUs = 0;
        }
    }

    private void startLoopingEffectNow(String resourcePath) {
        loopingEffectStopped = false;
        URL effectUrl = MusicHandler.class.getResource(resourcePath);
        if (effectUrl == null) {
            GameExceptions.showErrorDialog("Looping sound effect not found: " + resourcePath);
            return;
        }
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(effectUrl);
            Clip effectClip = AudioSystem.getClip();
            effectClip.open(audioStream);
            loopingEffectClip = effectClip;
            effectClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            GameExceptions.handleWithDialog("Failed to start looping sound effect", e);
        }
    }

    private void syncedLoopNow(String resourcePath, long periodMs) {
        URL effectUrl = MusicHandler.class.getResource(resourcePath);
        if (effectUrl == null) {
            GameExceptions.showErrorDialog("Looping sound effect not found: " + resourcePath);
            return;
        }
        while (!loopingEffectStopped) {
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(effectUrl);
                Clip effectClip = AudioSystem.getClip();
                effectClip.open(audioStream);
                loopingEffectClip = effectClip;
                effectClip.start();
                long cycleStart = System.currentTimeMillis();
                while (!loopingEffectStopped) {
                    long remaining = periodMs - (System.currentTimeMillis() - cycleStart);
                    if (remaining <= 0) break;
                    Thread.sleep(Math.min(50L, remaining));
                }
                effectClip.stop();
                effectClip.close();
                loopingEffectClip = null;
            } catch (InterruptedException e) {
                GameExceptions.handleInterrupted("Synced looping effect", e);
                break;
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                GameExceptions.handleWithDialog("Failed to play synced looping effect", e);
                break;
            }
        }
    }

    private void playEffectNow(String resourcePath) {
        URL effectUrl = MusicHandler.class.getResource(resourcePath);
        if (effectUrl == null) {
            GameExceptions.showErrorDialog("Sound effect not found: " + resourcePath);
            return;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(effectUrl);
            Clip effectClip = AudioSystem.getClip();
            effectClip.open(audioStream);
            effectClip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    effectClip.close();
                }
            });
            effectClip.start();
            // Thread returns to pool immediately; LineListener closes clip when playback ends
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            GameExceptions.handleWithDialog("Failed to play sound effect", e);
        }
    }

    private boolean isMidiResource(String resourcePath) {
        if (resourcePath == null) {
            return false;
        }
        String normalized = resourcePath.toLowerCase(Locale.ROOT);
        return normalized.endsWith(".mid") || normalized.endsWith(".midi");
    }
}
