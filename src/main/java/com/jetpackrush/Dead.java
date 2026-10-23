package com.jetpackrush;

/**
 * État final du joueur mort : il ignore l'entrée, retombe et reste mort.
 */
public class Dead implements PlayerState {
    /**
     * Applique seulement la gravité, sans poussée possible.
     *
     * @param player le joueur à mettre à jour
     * @param dt     temps écoulé depuis la dernière image, en secondes
     */
    @Override
    public void update(Player player, double dt) {
        player.applyPhysics(dt, false);
    }
}
