package com.jetpackrush;

/**
 * Réglages du mode Rush : une partie accélérée pensée pour la démo.
 *
 * <p>Le mode Rush ne change pas les règles : il fait passer le temps de jeu plus vite, donc tous les
 * obstacles se débloquent en quelques secondes et la vitesse monte plus vite. Il raccourcit aussi les
 * décors (ils changent souvent) et fait apparaître pièces et power-ups plus souvent. Le mode normal
 * n'utilise aucune de ces valeurs.
 */
public final class RushMode {

    /** Le temps de jeu passe 3 fois plus vite : les obstacles se débloquent 3 fois plus tôt. */
    public static final double TIME_FACTOR = 3;
    /** Longueur d'un décor en pixels (21 000 en mode normal) : il change toutes les 10 à 15 secondes. */
    public static final double ZONE_LENGTH = 4000;
    /** Les pièces et les power-ups apparaissent deux fois plus souvent. */
    public static final double SPAWN_SCALE = 0.5;
    /** Délai minimal entre deux obstacles, en secondes (1,1 en mode normal). */
    public static final double MIN_DELAY = 0.9;
    /** Vitesse maximale, en pixels par seconde (520 en mode normal). */
    public static final double MAX_SPEED = 560;
    /** Nombre de couloirs et de portes autorisés par partie (3 en mode normal). */
    public static final int MAX_PATTERNS = 99;
    /** Nombre de doubles lasers autorisés par partie (2 ou 3 en mode normal). */
    public static final int MAX_DOUBLES = 99;

    private RushMode() {
    }
}
