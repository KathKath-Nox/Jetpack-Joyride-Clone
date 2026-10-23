package com.jetpackrush;

import java.util.ArrayList;
import java.util.List;

/**
 * Compte la distance parcourue et les pièces ramassées, et prévient ses {@link ScoreListener}
 * (Observer) quand l'une des deux valeurs change.
 */
public class ScoreManager {

    /** Nombre de pixels de défilement pour un mètre affiché. */
    static final double PIXELS_PER_METER = 40;

    private final List<ScoreListener> listeners = new ArrayList<>();
    private double distancePixels;
    private int coins;

    /**
     * Abonne un observateur, qui reçoit tout de suite l'état courant.
     *
     * @param listener l'observateur à prévenir
     */
    public void addListener(ScoreListener listener) {
        listeners.add(listener);
        listener.onScoreChanged(getDistance(), coins);
    }

    /**
     * Désabonne un observateur.
     *
     * @param listener l'observateur à retirer
     */
    public void removeListener(ScoreListener listener) {
        listeners.remove(listener);
    }

    /**
     * Ajoute de la distance parcourue ; les observateurs ne sont prévenus que si le nombre
     * de mètres entiers change.
     *
     * @param pixels distance de défilement à ajouter, en pixels (positive)
     */
    public void addDistance(double pixels) {
        if (pixels <= 0) {
            return;
        }
        int before = getDistance();
        distancePixels += pixels;
        if (getDistance() != before) {
            notifyListeners();
        }
    }

    /** Ajoute une pièce ramassée. */
    public void addCoin() {
        coins++;
        notifyListeners();
    }

    /**
     * Donne la distance parcourue.
     *
     * @return la distance en mètres entiers
     */
    public int getDistance() {
        return (int) (distancePixels / PIXELS_PER_METER);
    }

    /**
     * Donne le nombre de pièces ramassées.
     *
     * @return le nombre de pièces
     */
    public int getCoins() {
        return coins;
    }

    private void notifyListeners() {
        int distance = getDistance();
        for (ScoreListener listener : List.copyOf(listeners)) {
            listener.onScoreChanged(distance, coins);
        }
    }
}
