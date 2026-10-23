package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Petite particule : un débris carré qui tourne, ou un rond qui grossit ou rétrécit. Elle a une
 * vitesse, une gravité et une durée de vie, et devient transparente en vieillissant.
 */
public class Particle {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private double angle;
    private double age;
    private final double gravity;
    private final double size;
    private final double growth;
    private final double spin;
    private final double lifetime;
    private final Color color;
    private final boolean debris;

    /**
     * Crée une particule.
     *
     * @param x        position horizontale initiale
     * @param y        position verticale initiale
     * @param vx       vitesse horizontale, en pixels par seconde
     * @param vy       vitesse verticale, en pixels par seconde
     * @param gravity  accélération verticale, en pixels par seconde au carré
     * @param size     taille initiale, en pixels
     * @param growth   variation de la taille par seconde (négative pour rétrécir)
     * @param spin     vitesse de rotation, en radians par seconde (débris seulement)
     * @param lifetime durée de vie, en secondes
     * @param color    couleur de la particule
     * @param debris   {@code true} pour un carré qui tourne, {@code false} pour un rond
     */
    public Particle(double x, double y, double vx, double vy, double gravity,
                    double size, double growth, double spin, double lifetime,
                    Color color, boolean debris) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.gravity = gravity;
        this.size = size;
        this.growth = growth;
        this.spin = spin;
        this.lifetime = lifetime;
        this.color = color;
        this.debris = debris;
    }

    /**
     * Fait vieillir la particule et la déplace.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    public void update(double dt) {
        age += dt;
        vy += gravity * dt;
        x += vx * dt;
        y += vy * dt;
        angle += spin * dt;
    }

    /**
     * Indique si la particule a encore de la vie.
     *
     * @return {@code true} tant que son âge est inférieur à sa durée de vie
     */
    public boolean isAlive() {
        return age < lifetime;
    }

    /**
     * Donne l'opacité de la particule, qui diminue avec l'âge.
     *
     * @return une valeur entre 1 (neuve) et 0 (fin de vie)
     */
    public double getAlpha() {
        return Math.max(0, 1 - age / lifetime);
    }

    /**
     * Donne la position horizontale.
     *
     * @return la position horizontale en pixels
     */
    public double getX() {
        return x;
    }

    /**
     * Donne la position verticale.
     *
     * @return la position verticale en pixels
     */
    public double getY() {
        return y;
    }

    /**
     * Dessine la particule avec son opacité courante.
     *
     * @param gc contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        double s = Math.max(0, size + growth * age);
        gc.save();
        gc.setGlobalAlpha(debris ? getAlpha() : getAlpha() * 0.8);
        gc.setFill(color);
        if (debris) {
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(angle));
            gc.fillRect(-s / 2, -s / 2, s, s);
        } else {
            gc.fillOval(x - s / 2, y - s / 2, s, s);
        }
        gc.restore();
    }
}
