package com.jetpackrush;

import javafx.scene.canvas.GraphicsContext;

/**
 * Un écran du jeu (pattern State) : menu, partie, pause ou fin de partie.
 * {@link Game} délègue chaque frame à l'état courant.
 */
public interface GameState {

    /**
     * Met à jour l'écran.
     *
     * @param dt temps écoulé depuis la frame précédente, en secondes
     */
    void update(double dt);

    /**
     * Dessine l'écran.
     *
     * @param gc le contexte graphique du canvas
     */
    void render(GraphicsContext gc);
}
