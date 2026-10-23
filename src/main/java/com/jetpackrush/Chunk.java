package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Tronçon préfabriqué : un groupe d'obstacles rectangulaires placés les uns par rapport aux autres.
 *
 * <p>Un tronçon doit toujours pouvoir se traverser : {@link #isPassable()} le vérifie, et
 * {@link LevelGenerator} refuse les tronçons qui échouent.
 */
public class Chunk {

    /** Un rectangle du tronçon, en pixels, relatif au bord gauche du tronçon. */
    public record Rect(double x, double y, double width, double height) {
    }

    /** Haut de la zone de jeu. */
    static final double PLAY_TOP = 0;
    /** Bas de la zone de jeu (le sol). */
    static final double PLAY_BOTTOM = App.HEIGHT - Decor.FLOOR_HEIGHT;
    /** Hauteur libre minimale pour passer : le joueur (60 px) plus une marge. */
    static final double MIN_GAP = 100;

    private final String name;
    private final double width;
    private final List<Rect> rects;

    /**
     * @param name  nom du tronçon, pour les messages d'erreur
     * @param width largeur du tronçon
     * @param rects les rectangles, qui doivent tenir dans la largeur du tronçon
     */
    public Chunk(String name, double width, List<Rect> rects) {
        for (Rect r : rects) {
            if (r.x() < 0 || r.x() + r.width() > width) {
                throw new IllegalArgumentException("Rectangle hors du tronçon " + name);
            }
        }
        this.name = name;
        this.width = width;
        this.rects = List.copyOf(rects);
    }

    /**
     * Donne le nom du tronçon.
     *
     * @return le nom du tronçon
     */
    public String getName() {
        return name;
    }

    /**
     * Donne la largeur du tronçon.
     *
     * @return la largeur, en pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * Donne les rectangles qui composent le tronçon.
     *
     * @return la liste des rectangles, non modifiable
     */
    public List<Rect> getRects() {
        return rects;
    }

    /**
     * Crée les obstacles du tronçon.
     *
     * @param originX position du bord gauche du tronçon
     * @param speed   vitesse de défilement du jeu
     * @return les obstacles, prêts à être ajoutés à la partie
     */
    public List<Obstacle> spawn(double originX, GameSpeed speed) {
        List<Obstacle> obstacles = new ArrayList<>();
        for (Rect r : rects) {
            obstacles.add(new DummyObstacle(originX + r.x(), r.y(), r.width(), r.height(), speed));
        }
        return obstacles;
    }

    /**
     * Vérifie qu'un chemin traverse le tronçon de bout en bout.
     *
     * <p>Le tronçon est découpé en tranches verticales. Dans chaque tranche on cherche les
     * passages libres d'au moins {@link #MIN_GAP} de haut ; on passe d'une tranche à la
     * suivante par un passage qui recoupe, sur au moins {@link #MIN_GAP}, un passage déjà atteint.
     *
     * @return {@code true} si le joueur peut traverser le tronçon
     */
    public boolean isPassable() {
        TreeSet<Double> cuts = new TreeSet<>();
        cuts.add(0.0);
        cuts.add(width);
        for (Rect r : rects) {
            cuts.add(r.x());
            cuts.add(r.x() + r.width());
        }
        List<double[]> reachable = null;
        Double from = null;
        for (double cut : cuts) {
            if (from != null) {
                List<double[]> gaps = freeGaps(from, cut);
                if (reachable != null) {
                    gaps = connected(gaps, reachable);
                }
                if (gaps.isEmpty()) {
                    return false;
                }
                reachable = gaps;
            }
            from = cut;
        }
        return true;
    }

    private List<double[]> freeGaps(double left, double right) {
        List<double[]> blocked = new ArrayList<>();
        for (Rect r : rects) {
            if (r.x() < right && r.x() + r.width() > left) {
                blocked.add(new double[] {r.y(), r.y() + r.height()});
            }
        }
        blocked.sort((a, b) -> Double.compare(a[0], b[0]));
        List<double[]> gaps = new ArrayList<>();
        double cursor = PLAY_TOP;
        for (double[] b : blocked) {
            addGap(gaps, cursor, b[0]);
            cursor = Math.max(cursor, b[1]);
        }
        addGap(gaps, cursor, PLAY_BOTTOM);
        return gaps;
    }

    private static void addGap(List<double[]> gaps, double top, double bottom) {
        if (bottom - top >= MIN_GAP) {
            gaps.add(new double[] {top, bottom});
        }
    }

    private static List<double[]> connected(List<double[]> gaps, List<double[]> reachable) {
        List<double[]> result = new ArrayList<>();
        for (double[] g : gaps) {
            for (double[] r : reachable) {
                double overlap = Math.min(g[1], r[1]) - Math.max(g[0], r[0]);
                if (overlap >= MIN_GAP) {
                    result.add(g);
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return name;
    }
}
