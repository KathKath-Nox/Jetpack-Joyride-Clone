package com.jetpackrush;

/** Joue des sons du dossier {@code assets}. */
public interface SoundPlayer {

    /**
     * Joue un son depuis le début.
     *
     * @param name nom du fichier, par exemple {@code coin_pickup_1.wav}
     */
    void play(String name);

    /**
     * Joue un son en boucle jusqu'à {@link #stop(String)}.
     *
     * @param name   nom du fichier
     * @param gainDb volume en décibels, 0 pour le volume d'origine, négatif pour baisser
     */
    void loop(String name, float gainDb);

    /**
     * Change le volume d'un son sans le relancer.
     *
     * @param name   nom du fichier
     * @param gainDb volume en décibels
     */
    void setGain(String name, float gainDb);

    /**
     * Arrête un son joué en boucle ou en cours.
     *
     * @param name nom du fichier
     */
    void stop(String name);
}
