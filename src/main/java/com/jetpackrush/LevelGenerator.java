package com.jetpackrush;

import java.util.List;
import java.util.Random;

/**
 * Générateur de niveau : enchaîne des {@link Chunk tronçons} préfabriqués tirés au hasard,
 * séparés par un espace libre. Tous les tronçons sont vérifiés franchissables à la création.
 *
 * <p>Le générateur ne connaît pas la partie : {@link #update(double)} renvoie les obstacles à
 * ajouter, c'est à l'appelant de les mettre à jour, les dessiner et les retirer.
 */
public class LevelGenerator {

    /** Où apparaît le bord gauche d'un tronçon : juste hors de l'écran à droite. */
    static final double SPAWN_X = App.WIDTH + 50;
    /** Espace libre entre deux tronçons, en pixels. */
    static final double GAP = 450;
    /** Distance de défilement avant le premier tronçon, en pixels. */
    static final double FIRST_DISTANCE = 600;

    private static final double H = Chunk.PLAY_BOTTOM;

    private final GameSpeed speed;
    private final Random random;
    private final List<Chunk> chunks;
    private double remaining = FIRST_DISTANCE;
    private Chunk last;

    /**
     * @param speed  vitesse de défilement du jeu
     * @param random source de hasard (fixée dans les tests)
     * @param chunks les tronçons possibles, au moins un, tous franchissables
     * @throws IllegalArgumentException si la liste est vide ou si un tronçon est infranchissable
     */
    public LevelGenerator(GameSpeed speed, Random random, List<Chunk> chunks) {
        if (chunks.isEmpty()) {
            throw new IllegalArgumentException("Il faut au moins un tronçon");
        }
        for (Chunk c : chunks) {
            if (!c.isPassable()) {
                throw new IllegalArgumentException("Tronçon infranchissable : " + c);
            }
        }
        this.speed = speed;
        this.random = random;
        this.chunks = List.copyOf(chunks);
    }

    /**
     * Crée un générateur avec les tronçons de base.
     *
     * @param speed  vitesse de défilement du jeu
     * @param random source de hasard
     * @return le générateur
     */
    public static LevelGenerator withDefaultChunks(GameSpeed speed, Random random) {
        return new LevelGenerator(speed, random, defaultChunks());
    }

    /**
     * Les tronçons de base, tous faits de rectangles factices.
     *
     * @return la liste des tronçons préfabriqués
     */
    static List<Chunk> defaultChunks() {
        return List.of(
            new Chunk("bloc-milieu", 60, List.of(new Chunk.Rect(0, 260, 60, 120))),
            new Chunk("bloc-sol", 80, List.of(new Chunk.Rect(0, H - 160, 80, 160))),
            new Chunk("bloc-plafond", 80, List.of(new Chunk.Rect(0, 0, 80, 220))),
            new Chunk("couloir", 400, List.of(
                new Chunk.Rect(0, 0, 400, 250),
                new Chunk.Rect(0, 450, 400, H - 450))),
            new Chunk("escalier", 420, List.of(
                new Chunk.Rect(0, 0, 60, 300),
                new Chunk.Rect(180, H - 300, 60, 300),
                new Chunk.Rect(360, 0, 60, 300))));
    }

    /**
     * Fait avancer le niveau.
     *
     * @param dt temps écoulé depuis la frame précédente, en secondes
     * @return les obstacles du tronçon qui vient d'apparaître, vide la plupart du temps
     */
    public List<Obstacle> update(double dt) {
        remaining -= speed.getSpeed() * dt;
        if (remaining > 0) {
            return List.of();
        }
        Chunk chunk = pick();
        // Le dépassement est repris dans la position, pour garder des espaces réguliers
        double originX = SPAWN_X - remaining;
        remaining += chunk.getWidth() + GAP;
        last = chunk;
        return chunk.spawn(originX, speed);
    }

    private Chunk pick() {
        if (chunks.size() == 1) {
            return chunks.get(0);
        }
        Chunk chunk;
        do {
            chunk = chunks.get(random.nextInt(chunks.size()));
        } while (chunk == last);
        return chunk;
    }
}
