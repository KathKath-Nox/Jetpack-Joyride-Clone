package com.jetpackrush;

/**
 * État du joueur qui vole : la gravité et la poussée du jetpack s'appliquent. Repasse en
 * {@link Falling} dès que la poussée est relâchée.
 */
public class Flying implements PlayerState {
    /**
     * Repasse en chute si la poussée est relâchée, sinon applique la gravité et la poussée.
     *
     * @param player le joueur à mettre à jour
     * @param dt     temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(Player player, double dt) {
        if (!player.isThrustPressed()) {
            player.setState(new Falling());
            player.getState().update(player, dt);
            return;
        }
        player.applyPhysics(dt, true);
    }
}
