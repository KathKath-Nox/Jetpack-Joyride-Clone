package com.jetpackrush;

/**
 * Contrat des objets avec lesquels le joueur peut entrer en contact. Chacun réagit à sa façon :
 * un obstacle blesse le joueur, un power-up lui donne un effet.
 */
public interface Collidable {
    /**
     * Appelée quand le joueur touche cet objet.
     *
     * @param player le joueur concerné
     */
    void onCollision(Player player);
}
