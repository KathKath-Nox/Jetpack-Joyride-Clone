package com.jetpackrush;

/**
 * État du joueur (pattern State). Chaque état décide du comportement du joueur à chaque image
 * et peut le faire passer dans un autre état.
 */
public interface PlayerState {
    /**
     * Applique le comportement de cet état pour une image.
     *
     * @param player le joueur à mettre à jour
     * @param dt     temps écoulé depuis la dernière image, en secondes
     */
    void update(Player player, double dt);
}
