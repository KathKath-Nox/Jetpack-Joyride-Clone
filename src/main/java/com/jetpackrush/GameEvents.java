package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;

/**
 * Sujet du pattern Observer pour les événements de partie : le joueur et la partie publient,
 * les {@link GameEventListener} (sons, par exemple) réagissent sans que les deux se connaissent.
 */
public class GameEvents {

    private final List<GameEventListener> listeners = new ArrayList<>();

    /**
     * Abonne un observateur.
     *
     * @param listener l'observateur à prévenir
     */
    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Désabonne un observateur.
     *
     * @param listener l'observateur à retirer
     */
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Prévient tous les observateurs.
     *
     * @param event ce qui vient d'arriver
     */
    public void publish(GameEvent event) {
        for (GameEventListener listener : List.copyOf(listeners)) {
            listener.onEvent(event);
        }
    }
}
