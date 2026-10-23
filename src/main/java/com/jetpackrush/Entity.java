package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;

/**
 * Classe de base de tout ce qui bouge ou s'affiche dans le jeu : le joueur, les items, les obstacles.
 * Elle porte une position et une taille, et impose à chaque sous-classe de savoir se mettre à jour
 * et se dessiner.
 */
public abstract class Entity {
    /** Position horizontale du coin supérieur gauche, en pixels. */
    protected double x;
    /** Position verticale du coin supérieur gauche, en pixels. */
    protected double y;
    /** Largeur de l'entité, en pixels. */
    protected double width;
    /** Hauteur de l'entité, en pixels. */
    protected double height;

    /**
     * Crée une entité à une position donnée avec une taille donnée.
     *
     * @param x      position horizontale du coin supérieur gauche
     * @param y      position verticale du coin supérieur gauche
     * @param width  largeur en pixels
     * @param height hauteur en pixels
     */
    protected Entity(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Met à jour l'entité pour une image.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    public abstract void update(double dt);

    /**
     * Dessine l'entité.
     *
     * @param gc contexte graphique du canvas
     */
    public abstract void render(GraphicsContext gc);

    /**
     * Donne la zone de collision de l'entité.
     *
     * @return une nouvelle {@link Hitbox} à la position et à la taille actuelles
     */
    public Hitbox getHitbox() {
        return new Hitbox(x, y, width, height);
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
}
