package com.jetpackrush;

/**
 * Source d'entrée du joueur (pattern Adapter). Le joueur lit ses commandes uniquement par cette
 * interface, jamais directement le clavier : une manette ou un téléphone peut donc la remplacer.
 */
public interface InputSource {
    /**
     * Indique si la poussée du jetpack est demandée.
     *
     * @return {@code true} tant que la touche de poussée est maintenue
     */
    boolean isThrustPressed();

    /**
     * Indique si la pause vient d'être demandée. Vaut {@code true} seulement à l'image où l'appui
     * commence. Par défaut {@code false}, pour que les entrées sans touche pause (comme les tests,
     * qui créent un {@code InputSource} avec une simple lambda) n'aient rien à écrire.
     *
     * @return {@code true} à l'image où la pause est demandée
     */
    default boolean isPausePressed() {
        return false;
    }
}
