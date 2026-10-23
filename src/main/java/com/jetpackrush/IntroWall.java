package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Mur d'entrée du début de partie, dessiné en code. Il est juste devant le joueur et éclate en morceaux
 * dès qu'il le touche. Il va du plafond jusqu'au sol, donc on ne peut pas passer par-dessus.
 */
public class IntroWall extends Entity {

    /** Largeur du mur, en pixels. */
    public static final double WIDTH = 90;
    /** Hauteur du mur, du plafond jusqu'au sol, en pixels. */
    public static final double HEIGHT = App.HEIGHT - Decor.FLOOR_HEIGHT;
    /** Nombre de colonnes de morceaux quand le mur éclate. */
    public static final int COLUMNS = 4;
    /** Nombre de lignes de morceaux quand le mur éclate. */
    public static final int ROWS = 14;

    private static final double GRAVITY = 1500;
    private static final double LIFETIME = 1.6;
    private static final Color PANEL = Color.web("#42577d");
    private static final Color FRAME = Color.web("#7e90b0");
    private static final Color DARK = Color.web("#2a3a5c");
    private static final Color STRIPE = Color.web("#a39a2c");
    private static final Color[] CHUNK_COLORS = {PANEL, DARK, FRAME};

    private enum State {
        STANDING,
        BROKEN
    }

    private static final class Fragment {
        private double x;
        private double y;
        private double vx;
        private double vy;
        private double angle;
        private double spin;
        private Color color;
    }

    private final GameSpeed speed;
    private final List<Fragment> fragments = new ArrayList<>();
    private State state = State.STANDING;
    private double age;

    /**
     * Crée un mur debout.
     *
     * @param x abscisse du bord gauche, en pixels
     * @param speed vitesse de défilement du décor
     */
    public IntroWall(double x, GameSpeed speed) {
        super(x, 0, WIDTH, HEIGHT);
        this.speed = speed;
    }

    /**
     * Indique si le mur est encore debout.
     *
     * @return vrai tant que le mur n'a pas éclaté
     */
    public boolean isStanding() {
        return state == State.STANDING;
    }

    /**
     * Indique si le mur n'a plus rien à afficher.
     *
     * @return vrai quand les morceaux ont disparu, ou quand le mur est sorti de l'écran sans éclater
     */
    public boolean isFinished() {
        if (state == State.BROKEN) {
            return age >= LIFETIME;
        }
        return x + width < 0;
    }

    /**
     * Donne le nombre de morceaux projetés par l'éclatement.
     *
     * @return le nombre de morceaux (0 tant que le mur est debout)
     */
    public int fragmentCount() {
        return fragments.size();
    }

    /**
     * Teste si le joueur touche le mur. Un mur éclaté ne bloque plus.
     *
     * @param player zone occupée par le joueur
     * @return vrai si le mur est debout et touche le joueur
     */
    public boolean touches(Hitbox player) {
        return state == State.STANDING && getHitbox().intersects(player);
    }

    /**
     * Fait éclater le mur en morceaux qui volent vers l'avant. Sans effet si le mur est déjà cassé.
     *
     * @param random source de hasard pour la direction, la rotation et la couleur des morceaux
     */
    public void shatter(Random random) {
        if (state == State.BROKEN) {
            return;
        }
        state = State.BROKEN;
        age = 0;
        double chunkWidth = width / COLUMNS;
        double chunkHeight = height / ROWS;
        for (int column = 0; column < COLUMNS; column++) {
            for (int row = 0; row < ROWS; row++) {
                Fragment fragment = new Fragment();
                fragment.x = x + column * chunkWidth;
                fragment.y = y + row * chunkHeight;
                fragment.vx = 80 + random.nextDouble() * 520;
                fragment.vy = -500 + random.nextDouble() * 650;
                fragment.spin = (random.nextDouble() - 0.5) * 1440;
                fragment.color = CHUNK_COLORS[random.nextInt(CHUNK_COLORS.length)];
                fragments.add(fragment);
            }
        }
    }

    /**
     * Fait avancer le mur vers le joueur, ou fait voler ses morceaux s'il a éclaté.
     *
     * @param dt temps écoulé depuis la frame précédente, en secondes
     */
    @Override
    public void update(double dt) {
        if (state == State.STANDING) {
            x -= speed.getSpeed() * dt;
            return;
        }
        age += dt;
        for (Fragment fragment : fragments) {
            fragment.vy += GRAVITY * dt;
            fragment.x += fragment.vx * dt;
            fragment.y += fragment.vy * dt;
            fragment.angle += fragment.spin * dt;
        }
    }

    /**
     * Dessine le mur, ou ses morceaux s'il a éclaté.
     *
     * @param gc contexte graphique du canvas
     */
    @Override
    public void render(GraphicsContext gc) {
        if (state == State.STANDING) {
            renderWall(gc);
        } else if (!isFinished()) {
            renderFragments(gc);
        }
    }

    private void renderWall(GraphicsContext gc) {
        double left = Math.floor(x);
        gc.setFill(PANEL);
        gc.fillRect(left, y, width, height);
        gc.setFill(FRAME);
        gc.fillRect(left, y, 10, height);
        gc.fillRect(left + width - 10, y, 10, height);
        gc.setFill(DARK);
        gc.fillRect(left + 10, y, 4, height);
        gc.fillRect(left + width - 14, y, 4, height);
        gc.setStroke(DARK);
        gc.setLineWidth(3);
        for (double lineY = 90; lineY < height; lineY += 90) {
            gc.strokeLine(left + 14, y + lineY, left + width - 14, y + lineY);
        }
        gc.setFill(STRIPE);
        gc.fillRect(left + 14, y + 400, width - 28, 4);
        gc.fillRect(left + 14, y + 411, width - 28, 20);
        gc.setFill(FRAME);
        for (double rivetY = 40; rivetY < height; rivetY += 90) {
            gc.fillOval(left + 18, y + rivetY, 6, 6);
            gc.fillOval(left + width - 24, y + rivetY, 6, 6);
        }
    }

    private void renderFragments(GraphicsContext gc) {
        double alpha = Math.max(0, 1 - age / LIFETIME);
        double chunkWidth = width / COLUMNS;
        double chunkHeight = height / ROWS;
        for (Fragment fragment : fragments) {
            gc.save();
            gc.translate(fragment.x + chunkWidth / 2, fragment.y + chunkHeight / 2);
            gc.rotate(fragment.angle);
            gc.setGlobalAlpha(alpha);
            gc.setFill(fragment.color);
            gc.fillRect(-chunkWidth / 2, -chunkHeight / 2, chunkWidth, chunkHeight);
            gc.restore();
        }
    }
}