package com.jetpackrush;

/**
 * Observateur du score : prévenu par {@link ScoreManager} à chaque changement.
 */
public interface ScoreListener {

    /**
     * Appelé quand la distance ou le nombre de pièces change.
     *
     * @param distance distance parcourue, en mètres
     * @param coins    nombre de pièces ramassées
     */
    void onScoreChanged(int distance, int coins);
}
