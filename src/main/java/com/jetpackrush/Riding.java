package com.jetpackrush;

/**
 * État du joueur dans le véhicule. Un appui court fait sauter depuis le sol, un appui maintenu plus
 * de {@code HOLD_DELAY} secondes active le jetpack du véhicule. Un compte à rebours ramène le joueur
 * en {@link Falling} à la fin.
 */
public class Riding implements PlayerState {
    /** Durée du véhicule, en secondes. */
    public static final double DURATION = 10;
    private static final double HOLD_DELAY = 0.2;

    private double timeLeft = DURATION;
    private double held;
    private boolean thrusting;

    /**
     * Gère le saut ou le vol du véhicule selon la durée d'appui, puis fait avancer le compte à
     * rebours. Passe en {@link Falling} quand le temps est écoulé.
     *
     * @param player le joueur à mettre à jour
     * @param dt     temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(Player player, double dt) {
        boolean pressed = player.isThrustPressed();
        held = pressed ? held + dt : 0;
        thrusting = pressed && held > HOLD_DELAY;
        if (thrusting) {
            player.applyPhysics(dt, true);
        } else {
            if (pressed && player.isOnGround()) {
                player.jump();
            }
            player.applyRidingPhysics(dt);
        }
        timeLeft -= dt;
        if (timeLeft <= 0) {
            player.setState(new Falling());
        }
    }

    /**
     * Donne le temps restant du véhicule.
     *
     * @return le temps restant en secondes
     */
    public double getTimeLeft() {
        return timeLeft;
    }

    /**
     * Indique si le jetpack du véhicule est actif (appui maintenu assez longtemps).
     *
     * @return {@code true} si le véhicule vole
     */
    public boolean isThrusting() {
        return thrusting;
    }
}
