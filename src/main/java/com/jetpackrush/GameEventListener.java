package com.jetpackrush;

/** Observateur des événements de partie, prévenu par {@link GameEvents}. */
public interface GameEventListener {

    /**
     * Appelé à chaque événement de partie.
     *
     * @param event ce qui vient d'arriver
     */
    void onEvent(GameEvent event);
}
