package com.jetpackrush;

import java.util.function.Supplier;

/** Fabrique (pattern Factory) qui construit l'obstacle correspondant à un {@link ObstacleType}. */
public class ObstacleFactory {
    private final GameSpeed speed;
    private final Supplier<Hitbox> target;

    /**
     * Crée une factory sans cible : les missiles ne suivent pas le joueur.
     *
     * @param speed vitesse de jeu partagée
     */
    public ObstacleFactory(GameSpeed speed) {
        this(speed, () -> null);
    }

    /**
     * Crée une factory avec une cible pour les missiles.
     *
     * @param speed vitesse de jeu partagée
     * @param target fournit la hitbox du joueur, que les missiles visent
     */
    public ObstacleFactory(GameSpeed speed, Supplier<Hitbox> target) {
        this.speed = speed;
        this.target = target;
    }

    /**
     * Construit un obstacle du type demandé.
     *
     * @param type type d'obstacle à créer
     * @param x position horizontale
     * @param y position verticale
     * @return le nouvel obstacle
     */
    public Obstacle create(ObstacleType type, double x, double y) {
        return switch (type) {
            case ZAPPER_HORIZONTAL -> new Zapper(Zapper.Orientation.HORIZONTAL, x, y, speed);
            case ZAPPER_VERTICAL -> new Zapper(Zapper.Orientation.VERTICAL, x, y, speed);
            case ZAPPER_DIAGONAL -> new Zapper(Zapper.Orientation.DIAGONAL, x, y, speed);
            case ZAPPER_HORIZONTAL_LONG -> new Zapper(Zapper.Orientation.HORIZONTAL_LONG, x, y, speed);
            case ZAPPER_VERTICAL_LONG -> new Zapper(Zapper.Orientation.VERTICAL_LONG, x, y, speed);
            case ZAPPER_ROTATING -> new Zapper(Zapper.Orientation.ROTATING, x, y, speed);
            case MISSILE -> new Missile(x, y, speed, target);
            case LASER -> new Laser(y, speed);
            case DUMMY -> new DummyObstacle(x, y, DummyObstacle.DEFAULT_SIZE, DummyObstacle.DEFAULT_SIZE, speed);
        };
    }
}