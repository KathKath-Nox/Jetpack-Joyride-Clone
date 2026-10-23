package com.jetpackrush;

import javafx.scene.paint.Color;

/**
 * Effet du véhicule : remplace le jetpack par le véhicule pendant {@link Riding#DURATION} secondes.
 */
public class VehicleEffect implements PowerUpEffect {
    /**
     * Passe le joueur dans le véhicule, sauf s'il est mort.
     *
     * @param player le joueur concerné
     */
    @Override
    public void apply(Player player) {
        if (!player.isDead()) {
            player.setState(new Riding());
        }
    }

    /**
     * Fait sortir le joueur du véhicule : il repasse en chute s'il y était.
     *
     * @param player le joueur concerné
     */
    @Override
    public void expire(Player player) {
        if (player.getState() instanceof Riding) {
            player.setState(new Falling());
        }
    }

    /**
     * Donne la couleur de repli du véhicule.
     *
     * @return orange
     */
    @Override
    public Color getColor() {
        return Color.ORANGE;
    }
}
