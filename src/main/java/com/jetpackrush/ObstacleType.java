package com.jetpackrush;

/** Les différents types d'obstacles que la {@link ObstacleFactory} sait fabriquer. */
public enum ObstacleType {
    /** Zapper horizontal. */
    ZAPPER_HORIZONTAL,
    /** Zapper vertical. */
    ZAPPER_VERTICAL,
    /** Zapper en diagonale. */
    ZAPPER_DIAGONAL,
    /** Zapper horizontal long. */
    ZAPPER_HORIZONTAL_LONG,
    /** Zapper vertical long. */
    ZAPPER_VERTICAL_LONG,
    /** Zapper qui tourne sur lui-même. */
    ZAPPER_ROTATING,
    /** Missile avec avertissement. */
    MISSILE,
    /** Laser avec avertissement. */
    LASER,
    /** Obstacle factice, utilisé pour les tests. */
    DUMMY
}