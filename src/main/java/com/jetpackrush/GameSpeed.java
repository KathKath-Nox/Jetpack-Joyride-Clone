package com.jetpackrush;

/** Source unique de la vitesse de défilement : décor, obstacles et pièces la lisent ici pour avancer au même rythme. */
public class GameSpeed {

    /** Vitesse de défilement par défaut, en pixels par seconde. */
    public static final double DEFAULT_SPEED = 300;

    private double speed;

    /** Crée une vitesse avec la valeur par défaut ({@link #DEFAULT_SPEED}). */
    public GameSpeed() {
        this(DEFAULT_SPEED);
    }

    /**
     * Crée une vitesse avec une valeur de départ.
     *
     * @param initialSpeed vitesse de départ, en pixels par seconde
     */
    public GameSpeed(double initialSpeed) {
        this.speed = checked(initialSpeed);
    }

    /**
     * @return la vitesse actuelle, en pixels par seconde
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Change la vitesse de défilement.
     *
     * @param speed nouvelle vitesse, en pixels par seconde
     */
    public void setSpeed(double speed) {
        this.speed = checked(speed);
    }

    private static double checked(double speed) {
        if (speed < 0) {
            throw new IllegalArgumentException("La vitesse ne peut pas être négative : " + speed);
        }
        return speed;
    }
}
