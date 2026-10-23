package com.jetpackrush;

/**
 * Difficulté progressive : fait monter la vitesse du décor par paliers jusqu'à un plafond.
 *
 * <p>Seule classe qui écrit dans {@link GameSpeed} pendant la partie : tout ce qui défile
 * lit cette vitesse, donc un palier accélère le décor, les obstacles et les items en même temps.
 */
public class DifficultyManager {

    /** Vitesse au début de la partie, en pixels par seconde (celle où l'intro s'arrête). */
    static final double START_SPEED = 220;
    /** Vitesse ajoutée à chaque palier, en pixels par seconde. */
    static final double STEP_GAIN = 60;
    /** Durée d'un palier, en secondes. */
    static final double STEP_TIME = 15;
    /** Vitesse maximale, en pixels par seconde. */
    static final double MAX_SPEED = 520;

    private final GameSpeed speed;
    private double elapsed;

    /**
     * @param speed la vitesse partagée que ce gestionnaire fait monter
     */
    public DifficultyManager(GameSpeed speed) {
        this.speed = speed;
    }

    /**
     * Vitesse du décor après {@code time} secondes de jeu.
     *
     * @param time temps de jeu en secondes (un temps négatif compte comme le début)
     * @return la vitesse du palier en cours, jamais au-dessus de {@link #MAX_SPEED}
     */
    static double speedAt(double time) {
        double steps = Math.floor(Math.max(0, time) / STEP_TIME);
        return Math.min(MAX_SPEED, START_SPEED + steps * STEP_GAIN);
    }

    /**
     * Avance le temps de jeu et écrit la vitesse du palier en cours dans {@link GameSpeed}.
     *
     * @param dt temps écoulé depuis la dernière frame, en secondes
     */
    public void update(double dt) {
        elapsed += dt;
        speed.setSpeed(speedAt(elapsed));
    }
}
