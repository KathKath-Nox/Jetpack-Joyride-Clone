package com.jetpackrush;

/**
 * Objet ramassable. C'est une {@link Entity} qui défile vers la gauche à la vitesse du jeu et qui
 * réagit au contact du joueur ({@link Collidable}).
 */
public abstract class Item extends Entity implements Collidable {
    private final GameSpeed speed;
    private boolean collected;

    /**
     * Crée un item.
     *
     * @param x      position horizontale du coin supérieur gauche
     * @param y      position verticale du coin supérieur gauche
     * @param width  largeur en pixels
     * @param height hauteur en pixels
     * @param speed  vitesse de défilement du jeu
     */
    protected Item(double x, double y, double width, double height, GameSpeed speed) {
        super(x, y, width, height);
        this.speed = speed;
    }

    /**
     * Fait défiler l'item vers la gauche à la vitesse du jeu.
     *
     * @param dt temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(double dt) {
        x -= speed.getSpeed() * dt;
    }

    /**
     * Marque l'item comme ramassé. Une fois ramassé, il ne peut plus l'être.
     */
    protected void collect() {
        collected = true;
    }

    /**
     * Indique si l'item a déjà été ramassé.
     *
     * @return {@code true} s'il est ramassé
     */
    public boolean isCollected() {
        return collected;
    }

    /**
     * Indique si l'item est entièrement sorti de l'écran par la gauche.
     *
     * @return {@code true} s'il est hors écran
     */
    public boolean isOffScreen() {
        return x + width < 0;
    }
}
