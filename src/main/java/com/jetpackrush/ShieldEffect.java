package com.jetpackrush;

/**
 * Effet du bouclier : protège le joueur et absorbe un choc mortel.
 */
public class ShieldEffect implements PowerUpEffect {
    /**
     * Donne le bouclier au joueur.
     *
     * @param player le joueur concerné
     */
    @Override
    public void apply(Player player) {
        player.giveShield();
    }

    /**
     * Retire le bouclier au joueur.
     *
     * @param player le joueur concerné
     */
    @Override
    public void expire(Player player) {
        player.removeShield();
    }
}
