package com.jetpackrush;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * {@link SoundPlayer} qui mélange tous les sons dans une seule sortie audio.
 *
 * <p>Pourquoi un mélangeur maison : beaucoup de cartes son (Linux surtout) ne laissent ouvrir
 * qu'<em>une</em> ligne à la fois. Avec un clip par son, la musique prenait la sortie et les
 * effets n'avaient plus nulle part où jouer. Ici une seule ligne est ouverte, et un fil additionne
 * les sons en cours (musique, jetpack, pièces...) par petits blocs.
 *
 * <p>Les sons sont lus avec {@code javax.sound} (plus vorbisspi pour les {@code .ogg}), convertis
 * en 44,1 kHz stéréo 16 bits au chargement, et chargés sur un fil à part pour ne pas figer le jeu.
 * Sans carte son, ou si un fichier est illisible, le son est ignoré : le jeu continue en silence.
 */
public class MixingSoundPlayer implements SoundPlayer {

    static final int RATE = 44100;
    static final AudioFormat FORMAT =
        new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, RATE, 16, 2, 4, RATE, false);

    /** Un son plus court que cela est un effet : son volume est normalisé au chargement. */
    static final double MAX_EFFECT_SECONDS = 5;
    private static final double TARGET_PEAK = 0.95 * Short.MAX_VALUE;
    private static final double MAX_BOOST = 8;
    private static final int CHUNK_FRAMES = 1024;
    private static final int BUFFER_FRAMES = 3072;
    private static final int MAX_VOICES = 24;

    /** Un son en train d'être joué. Seul le fil du mélangeur avance {@code pos}. */
    static final class Voice {
        final String name;
        final short[] data;
        final boolean loop;
        volatile float gain;
        volatile boolean stopped;
        int pos;

        Voice(String name, short[] data, boolean loop, float gain) {
            this.name = name;
            this.data = data;
            this.loop = loop;
            this.gain = gain;
        }
    }

    private final ExecutorService loader = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "sound-loader");
        thread.setDaemon(true);
        return thread;
    });
    // Touché uniquement par le fil « sound-loader »
    private final Map<String, short[]> sounds = new HashMap<>();
    private final Set<String> failed = new HashSet<>();

    private final List<Voice> voices = new CopyOnWriteArrayList<>();
    private Thread mixerThread;
    private volatile boolean unavailable;

    @Override
    public void play(String name) {
        loader.execute(() -> {
            short[] data = sound(name);
            if (data != null) {
                add(new Voice(name, data, false, 1f));
            }
        });
    }

    @Override
    public void loop(String name, float gainDb) {
        loader.execute(() -> {
            short[] data = sound(name);
            if (data != null) {
                stopVoices(name);
                add(new Voice(name, data, true, toLinear(gainDb)));
            }
        });
    }

    @Override
    public void setGain(String name, float gainDb) {
        loader.execute(() -> {
            for (Voice voice : voices) {
                if (voice.name.equals(name)) {
                    voice.gain = toLinear(gainDb);
                }
            }
        });
    }

    @Override
    public void stop(String name) {
        loader.execute(() -> stopVoices(name));
    }

    private void stopVoices(String name) {
        for (Voice voice : voices) {
            if (voice.name.equals(name)) {
                voice.stopped = true;
            }
        }
    }

    static float toLinear(float gainDb) {
        return (float) Math.pow(10, gainDb / 20.0);
    }

    private void add(Voice voice) {
        if (unavailable) {
            return;
        }
        if (voices.size() >= MAX_VOICES) {
            voices.stream().filter(v -> !v.loop).findFirst().ifPresent(v -> v.stopped = true);
        }
        voices.add(voice);
        startMixer();
    }

    private synchronized void startMixer() {
        if (mixerThread == null) {
            mixerThread = new Thread(this::runMixer, "sound-mixer");
            mixerThread.setDaemon(true);
            mixerThread.start();
        }
    }

    private void runMixer() {
        SourceDataLine line = openLine();
        if (line == null) {
            unavailable = true;
            voices.clear();
            System.err.println("Son indisponible : aucune sortie audio ne s'ouvre");
            return;
        }
        short[] chunk = new short[CHUNK_FRAMES * 2];
        byte[] bytes = new byte[chunk.length * 2];
        try {
            line.start();
            while (true) {
                mix(chunk, voices);
                for (int i = 0; i < chunk.length; i++) {
                    bytes[2 * i] = (byte) chunk[i];
                    bytes[2 * i + 1] = (byte) (chunk[i] >> 8);
                }
                line.write(bytes, 0, bytes.length);
            }
        } catch (RuntimeException e) {
            unavailable = true;
            voices.clear();
            System.err.println("Son interrompu : " + e);
        } finally {
            line.close();
        }
    }

    private static SourceDataLine openLine() {
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, FORMAT);
        for (Mixer mixer : rankedMixers(info)) {
            try {
                SourceDataLine line = (SourceDataLine) mixer.getLine(info);
                line.open(FORMAT, BUFFER_FRAMES * FORMAT.getFrameSize());
                System.err.println("Sortie audio : " + mixer.getMixerInfo().getName());
                return line;
            } catch (LineUnavailableException | IllegalArgumentException e) {
                // cette sortie ne s'ouvre pas : on essaie la suivante
            }
        }
        return null;
    }

    /**
     * Additionne les sons en cours dans un bloc de sortie (stéréo entrelacé), fait avancer chacun
     * et retire ceux qui sont arrêtés ou finis. Les boucles reprennent au début.
     *
     * @param out    le bloc à remplir (écrasé)
     * @param voices les sons en cours, modifiée pour retirer les sons terminés
     */
    static void mix(short[] out, List<Voice> voices) {
        float[] sum = new float[out.length];
        for (Voice voice : voices) {
            if (voice.stopped || voice.data.length == 0) {
                voices.remove(voice);
                continue;
            }
            float gain = voice.gain;
            int i = 0;
            while (i < sum.length) {
                if (voice.pos >= voice.data.length) {
                    if (!voice.loop) {
                        break;
                    }
                    voice.pos = 0;
                }
                sum[i++] += voice.data[voice.pos++] * gain;
            }
            if (!voice.loop && voice.pos >= voice.data.length) {
                voices.remove(voice);
            }
        }
        for (int i = 0; i < out.length; i++) {
            out[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(sum[i])));
        }
    }

    // Charge un son (une seule fois), ou renvoie null s'il est illisible
    private short[] sound(String name) {
        short[] cached = sounds.get(name);
        if (cached != null || failed.contains(name)) {
            return cached;
        }
        try {
            short[] data = load(name);
            sounds.put(name, data);
            return data;
        } catch (IOException | UnsupportedAudioFileException | RuntimeException e) {
            failed.add(name);
            System.err.println("Son indisponible (" + name + ") : " + e);
            return null;
        }
    }

    private static short[] load(String name) throws IOException, UnsupportedAudioFileException {
        try (InputStream in = new BufferedInputStream(Assets.open(name));
             AudioInputStream source = AudioSystem.getAudioInputStream(in)) {
            // PCM 16 bits : lisible depuis les .wav à virgule flottante comme depuis les .ogg
            AudioFormat f = source.getFormat();
            AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, f.getSampleRate(), 16,
                f.getChannels(), f.getChannels() * 2, f.getSampleRate(), false);
            try (AudioInputStream converted = AudioSystem.getAudioInputStream(pcm, source)) {
                byte[] bytes = converted.readAllBytes();
                short[] samples = new short[bytes.length / 2];
                for (int i = 0; i < samples.length; i++) {
                    samples[i] = (short) ((bytes[2 * i] & 0xff) | (bytes[2 * i + 1] << 8));
                }
                short[] stereo = toMixFormat(samples, f.getChannels(), f.getSampleRate());
                // Les effets sont enregistrés à des niveaux très différents (les pièces et le choc
                // très bas) : on les ramène au même niveau. La musique garde le sien.
                if (stereo.length / 2.0 / RATE < MAX_EFFECT_SECONDS) {
                    normalize(stereo);
                }
                return stereo;
            }
        }
    }

    /**
     * Convertit des échantillons en stéréo 44,1 kHz (interpolation linéaire).
     *
     * @param samples  les échantillons entrelacés, 1 ou 2 canaux
     * @param channels nombre de canaux de la source (1 ou 2)
     * @param rate     fréquence de la source, en Hz
     * @return les échantillons stéréo entrelacés à {@link #RATE} Hz
     */
    static short[] toMixFormat(short[] samples, int channels, float rate) {
        if (channels < 1 || channels > 2) {
            throw new IllegalArgumentException("Canaux non gérés : " + channels);
        }
        int frames = samples.length / channels;
        if (frames == 0) {
            return new short[0];
        }
        int outFrames = (int) Math.round(frames * (double) RATE / rate);
        short[] out = new short[outFrames * 2];
        for (int i = 0; i < outFrames; i++) {
            double position = i * (double) rate / RATE;
            int a = Math.min(frames - 1, (int) position);
            int b = Math.min(frames - 1, a + 1);
            double t = position - a;
            for (int c = 0; c < 2; c++) {
                int channel = channels == 1 ? 0 : c;
                double va = samples[a * channels + channel];
                double vb = samples[b * channels + channel];
                out[2 * i + c] = (short) Math.round(va + (vb - va) * t);
            }
        }
        return out;
    }

    /**
     * Amplifie des échantillons pour que leur pic atteigne 95 % du maximum. L'amplification est
     * plafonnée, pour ne pas gonfler un simple souffle.
     *
     * @param samples les échantillons, modifiés sur place
     */
    static void normalize(short[] samples) {
        int peak = 0;
        for (short s : samples) {
            peak = Math.max(peak, Math.abs((int) s));
        }
        if (peak == 0) {
            return;
        }
        double scale = Math.min(MAX_BOOST, TARGET_PEAK / peak);
        for (int i = 0; i < samples.length; i++) {
            long v = Math.round(samples[i] * scale);
            samples[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, v));
        }
    }

    /**
     * Les sorties audio capables de jouer le format, de la plus à la moins probable d'être la bonne.
     * {@code -Dsound.mixer=texte} force la première sortie dont le nom contient ce texte.
     */
    static List<Mixer> rankedMixers(DataLine.Info info) {
        String wanted = System.getProperty("sound.mixer", "").toLowerCase(Locale.ROOT);
        List<Mixer> mixers = new ArrayList<>();
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            if (mixer.isLineSupported(info)) {
                mixers.add(mixer);
            }
        }
        // sort est stable : à score égal, l'ordre du système est gardé
        mixers.sort(Comparator.comparingInt((Mixer m) -> -score(m, wanted)));
        return mixers;
    }

    private static int score(Mixer mixer, String wanted) {
        String text = (mixer.getMixerInfo().getName() + " " + mixer.getMixerInfo().getDescription())
            .toLowerCase(Locale.ROOT);
        int score = 0;
        if (!wanted.isEmpty() && text.contains(wanted)) {
            score += 1000;
        }
        // La sortie par défaut du système (Windows, Linux avec PulseAudio ou PipeWire)
        if (text.contains("primary sound driver") || text.contains("pulse") || text.contains("pipewire")
            || text.contains("default")) {
            score += 100;
        }
        if (text.contains("speaker") || text.contains("analog") || text.contains("headphone")) {
            score += 10;
        }
        // Sorties qui ne sont presque jamais celles qu'on écoute
        if (text.contains("hdmi") || text.contains("displayport") || text.contains("dmic")
            || text.contains("digital") || text.contains("spdif")) {
            score -= 50;
        }
        return score;
    }
}
