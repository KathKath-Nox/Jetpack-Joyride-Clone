package com.jetpackrush;

import javafx.scene.paint.Color;

/**
 * Effet d'un power-up (pattern Strategy). Chaque power-up délègue à un effet ce qu'il fait au joueur.
 */
public interface PowerUpEffect {
    /**
     * Applique l'effet au joueur quand il ramasse le power-up.
     *
     * @param player le joueur concerné
     */
    void apply(Player player);

    /**
     * Retire l'effet au joueur quand il se termine.
     *
     * @param player le joueur concerné
     */
    void expire(Player player);

    /**
     * Donne la couleur de repli de l'effet, utilisée quand l'image du power-up est introuvable.
     *
     * @return la couleur de l'effet, cyan par défaut
     */
    default Color getColor() {
        return Color.CYAN;
    }
}
