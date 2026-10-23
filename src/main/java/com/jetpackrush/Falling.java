package com.jetpackrush;

/**
 * État du joueur qui tombe : la gravité seule s'applique. Passe en {@link Flying} dès que la
 * poussée est demandée.
 */
public class Falling implements PlayerState {
    /**
     * Passe en vol si la poussée est demandée, sinon applique la gravité.
     *
     * @param player le joueur à mettre à jour
     * @param dt     temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(Player player, double dt) {
        if (player.isThrustPressed()) {
            player.setState(new Flying());
            player.getState().update(player, dt);
            return;
        }
        player.applyPhysics(dt, false);
    }
}
