package com.jetpackrush;

import java.util.List;

/**
 * Observateur qui joue les sons : un son par {@link GameEvent}, plus la musique du jeu et le
 * grondement du jetpack, qui tourne en boucle tant que le jetpack pousse. Il suit les
 * {@link SoundSettings} : volume et coupure de la musique, coupure des effets.
 */
public class SoundEffects implements GameEventListener {

    static final String COIN = "coin_pickup_1.wav";
    static final String POP = "dud_zapper_pop.wav";
    static final String SHOCK = "shock.wav";
    static final String JETPACK_START = "jetpack_start.wav";
    static final String JETPACK_LOOP = "jetpack_lp.wav";
    static final String JETPACK_STOP = "jetpack_stop.wav";
    static final String THEME = "theme.ogg";

    private static final float JETPACK_GAIN_DB = -9;

    private final SoundPlayer player;
    private final SoundSettings settings;
    private boolean musicStarted;
    private boolean musicPlaying;

    /**
     * @param player ce qui joue réellement les sons
     */
    public SoundEffects(SoundPlayer player) {
        this(player, new SoundSettings());
    }

    /**
     * @param player   ce qui joue réellement les sons
     * @param settings les réglages à suivre
     */
    public SoundEffects(SoundPlayer player, SoundSettings settings) {
        this.player = player;
        this.settings = settings;
        settings.addListener(this::applySettings);
    }

    @Override
    public void onEvent(GameEvent event) {
        if (!settings.isEffectsOn()) {
            // Un jetpack qui s'arrête doit toujours couper son grondement, même sans effets
            if (event == GameEvent.THRUST_STOPPED) {
                player.stop(JETPACK_LOOP);
            }
            return;
        }
        switch (event) {
            case COIN_COLLECTED -> player.play(COIN);
            case PROTECTION_LOST -> player.play(POP);
            case PLAYER_SHOCKED -> player.play(SHOCK);
            case THRUST_STARTED -> {
                player.play(JETPACK_START);
                player.loop(JETPACK_LOOP, JETPACK_GAIN_DB);
            }
            case THRUST_STOPPED -> {
                player.stop(JETPACK_LOOP);
                player.play(JETPACK_STOP);
            }
        }
    }

    /** Lance la musique du jeu, en boucle (si elle est activée dans les réglages). */
    public void startMusic() {
        musicStarted = true;
        applySettings();
    }

    /** Coupe le grondement du jetpack (pause, fin de partie, retour au menu). */
    public void silenceJetpack() {
        player.stop(JETPACK_LOOP);
    }

    // Met la musique en accord avec les réglages : volume, coupure, reprise
    private void applySettings() {
        if (!settings.isEffectsOn()) {
            silenceJetpack();
        }
        if (!musicStarted) {
            return;
        }
        if (!settings.isMusicAudible()) {
            if (musicPlaying) {
                player.stop(THEME);
                musicPlaying = false;
            }
        } else if (musicPlaying) {
            player.setGain(THEME, settings.getMusicGainDb());
        } else {
            player.loop(THEME, settings.getMusicGainDb());
            musicPlaying = true;
        }
    }

    /**
     * Les fichiers de son qu'un événement peut jouer.
     *
     * @param event l'événement
     * @return les fichiers, dans l'ordre où ils sont lancés
     */
    static List<String> soundsFor(GameEvent event) {
        return switch (event) {
            case COIN_COLLECTED -> List.of(COIN);
            case PROTECTION_LOST -> List.of(POP);
            case PLAYER_SHOCKED -> List.of(SHOCK);
            case THRUST_STARTED -> List.of(JETPACK_START, JETPACK_LOOP);
            case THRUST_STOPPED -> List.of(JETPACK_STOP);
        };
    }
}
