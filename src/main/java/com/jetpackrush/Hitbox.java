package com.jetpackrush;

/**
 * Rectangle de collision immuable. Toutes les collisions du jeu se font en comparant deux hitbox.
 */
public class Hitbox {
    private final double x;
    private final double y;
    private final double width;
    private final double height;

    /**
     * Crée une hitbox.
     *
     * @param x      position horizontale du coin supérieur gauche
     * @param y      position verticale du coin supérieur gauche
     * @param width  largeur en pixels
     * @param height hauteur en pixels
     */
    public Hitbox(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Donne la position horizontale.
     *
     * @return la position horizontale en pixels
     */
    public double getX() { return x; }

    /**
     * Donne la position verticale.
     *
     * @return la position verticale en pixels
     */
    public double getY() { return y; }

    /**
     * Donne la largeur.
     *
     * @return la largeur en pixels
     */
    public double getWidth() { return width; }

    /**
     * Donne la hauteur.
     *
     * @return la hauteur en pixels
     */
    public double getHeight() { return height; }

    /**
     * Indique si cette hitbox chevauche une autre. Deux hitbox qui se touchent seulement par un bord
     * ne se chevauchent pas.
     *
     * @param other l'autre hitbox
     * @return {@code true} si les deux rectangles se chevauchent
     */
    public boolean intersects(Hitbox other) {
        return x < other.x + other.width
            && x + width > other.x
            && y < other.y + other.height
            && y + height > other.y;
    }
}
