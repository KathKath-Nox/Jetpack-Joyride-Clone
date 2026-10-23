package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Réglages du son : volume de la musique, musique et effets activés ou non. Les observateurs
 * (voir {@link #addListener(Runnable)}) sont prévenus à chaque changement, et les réglages sont
 * réécrits dans un fichier si {@link #persistTo(Path)} a été appelé.
 */
public class SoundSettings {

    /** Pas du volume, en pourcentage. */
    static final int STEP = 10;
    /** Volume de la musique par défaut, en pourcentage. */
    static final int DEFAULT_VOLUME = 50;

    private final List<Runnable> listeners = new ArrayList<>();
    private Path file;
    private int musicVolume = DEFAULT_VOLUME;
    private boolean musicOn = true;
    private boolean effectsOn = true;

    /**
     * Crée des réglages en mémoire uniquement.
     */
    public SoundSettings() {
    }

    /**
     * Crée des réglages sauvegardés dans {@code .jetpackrush/sound.properties}, dans le dossier
     * personnel de l'utilisateur.
     *
     * @return les réglages, rechargés depuis le fichier s'il existe
     */
    public static SoundSettings persistent() {
        SoundSettings settings = new SoundSettings();
        settings.persistTo(Paths.get(System.getProperty("user.home"), ".jetpackrush", "sound.properties"));
        return settings;
    }

    /**
     * Recharge les réglages depuis un fichier (absent ou illisible : on garde les valeurs actuelles),
     * puis les y réécrit à chaque changement.
     *
     * @param file le fichier de réglages
     */
    public void persistTo(Path file) {
        this.file = file;
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
            musicVolume = clamp(Integer.parseInt(props.getProperty("music.volume", "" + musicVolume)));
            musicOn = Boolean.parseBoolean(props.getProperty("music.on", "" + musicOn));
            effectsOn = Boolean.parseBoolean(props.getProperty("effects.on", "" + effectsOn));
        } catch (IOException | IllegalArgumentException e) {
            // pas de fichier ou fichier abîmé : valeurs par défaut
        }
        notifyListeners();
    }

    /**
     * Abonne un observateur, prévenu à chaque changement de réglage.
     *
     * @param listener l'action à lancer
     */
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    /**
     * @return le volume de la musique, de 0 à 100
     */
    public int getMusicVolume() {
        return musicVolume;
    }

    /**
     * Change le volume de la musique.
     *
     * @param percent le volume, ramené entre 0 et 100
     */
    public void setMusicVolume(int percent) {
        int value = clamp(percent);
        if (value != musicVolume) {
            musicVolume = value;
            changed();
        }
    }

    /**
     * Monte ou baisse le volume de la musique.
     *
     * @param steps nombre de pas ({@link #STEP}), négatif pour baisser
     */
    public void changeMusicVolume(int steps) {
        setMusicVolume(musicVolume + steps * STEP);
    }

    /**
     * @return {@code true} si la musique est activée
     */
    public boolean isMusicOn() {
        return musicOn;
    }

    /**
     * Active ou coupe la musique.
     *
     * @param on {@code false} pour la couper
     */
    public void setMusicOn(boolean on) {
        if (on != musicOn) {
            musicOn = on;
            changed();
        }
    }

    /**
     * @return {@code true} si les effets sonores (pièces, chocs, jetpack) sont activés
     */
    public boolean isEffectsOn() {
        return effectsOn;
    }

    /**
     * Active ou coupe les effets sonores.
     *
     * @param on {@code false} pour les couper
     */
    public void setEffectsOn(boolean on) {
        if (on != effectsOn) {
            effectsOn = on;
            changed();
        }
    }

    /**
     * @return {@code true} si la musique doit se faire entendre : activée et volume au-dessus de 0
     */
    public boolean isMusicAudible() {
        return musicOn && musicVolume > 0;
    }

    /**
     * Convertit le volume de la musique en décibels.
     *
     * @return 0 dB à 100 %, de plus en plus négatif quand le volume baisse (volume 0 : -80 dB)
     */
    public float getMusicGainDb() {
        return musicVolume <= 0 ? -80f : (float) (20 * Math.log10(musicVolume / 100.0));
    }

    private static int clamp(int percent) {
        return Math.max(0, Math.min(100, percent));
    }

    private void changed() {
        save();
        notifyListeners();
    }

    private void notifyListeners() {
        for (Runnable listener : List.copyOf(listeners)) {
            listener.run();
        }
    }

    private void save() {
        if (file == null) {
            return;
        }
        Properties props = new Properties();
        props.setProperty("music.volume", "" + musicVolume);
        props.setProperty("music.on", "" + musicOn);
        props.setProperty("effects.on", "" + effectsOn);
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Jetpack Joyride : reglages du son");
            }
        } catch (IOException e) {
            // pas grave : les réglages restent valables jusqu'à la fermeture du jeu
        }
    }
}
