package com.jetpackrush;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import com.jetpackrush.KeyBindings.Action;

/**
 * Entrée clavier (pattern Adapter) : implémentation de {@link InputSource} qui lit les touches de la
 * scène JavaFX. Les touches de poussée du jetpack et de pause viennent de {@link KeyBindings}
 * (Espace et Échap par défaut, modifiables dans l'écran « Touches »). Elle sert aussi au menu, qui
 * réagit une seule fois par appui grâce à {@link #wasPressed(KeyCode)}.
 */
public class KeyboardInput implements InputSource {
    private final Set<KeyCode> pressed = new HashSet<>();
    private final KeyBindings bindings = new KeyBindings();

    // Pour le MENU : touches qui viennent d'être appuyées, pour réagir une seule fois par appui
    // (sinon garder ↓ enfoncé ferait défiler la sélection à chaque frame)
    private final Set<KeyCode> justPressed = new HashSet<>();
    private final Set<KeyCode> frameJustPressed = new HashSet<>();
    private boolean thrustLocked;
    // Touches simulées par le téléphone : remplies depuis le fil du serveur, lues dans poll()
    private final ConcurrentLinkedQueue<KeyCode> injected = new ConcurrentLinkedQueue<>();

    /**
     * Crée une entrée clavier branchée sur une scène : les appuis et relâchements de touches de la
     * scène sont enregistrés.
     *
     * @param scene la scène dont on lit le clavier
     */
    public KeyboardInput(Scene scene) {
        scene.setOnKeyPressed(e -> press(e.getCode()));
        scene.setOnKeyReleased(e -> release(e.getCode()));
    }

    KeyboardInput() {
    }

    void press(KeyCode key) {
        // add() renvoie false si la touche est déjà maintenue (répétition automatique du clavier)
        if (pressed.add(key)) {
            justPressed.add(key);
        }
    }

    void release(KeyCode key) {
        pressed.remove(key);
        if (key == bindings.get(Action.THRUST)) {
            thrustLocked = false;
        }
    }

    /** Relâche toutes les touches (fenêtre qui perd le focus : les relâchements ne sont plus reçus). */
    void releaseAll() {
        pressed.clear();
        thrustLocked = false;
    }

    /**
     * Ignore le jetpack tant qu'Espace n'a pas été relâché, pour que l'appui qui a validé
     * « Jouer » ou « Rejouer » ne fasse pas décoller le joueur au début de la partie.
     */
    void lockThrust() {
        thrustLocked = pressed.contains(bindings.get(Action.THRUST));
    }

    /**
     * Simule un appui de touche (manette téléphone). Peut être appelé depuis n'importe quel fil :
     * l'appui est pris en compte au prochain {@link #poll()}.
     *
     * @param key la touche appuyée
     */
    public void inject(KeyCode key) {
        injected.add(key);
    }

    /** À appeler une fois par frame, avant les update. */
    public void poll() {
        KeyCode key;
        while ((key = injected.poll()) != null) {
            justPressed.add(key);
        }
        frameJustPressed.clear();
        frameJustPressed.addAll(justPressed);
        justPressed.clear();
    }

    /**
     * Utilisé par le menu pour naviguer.
     *
     * @param key la touche à tester
     * @return true seulement à la frame où l'appui commence
     */
    public boolean wasPressed(KeyCode key) {
        return frameJustPressed.contains(key);
    }

    @Override
    public boolean isThrustPressed() {
        return !thrustLocked && pressed.contains(bindings.get(Action.THRUST));
    }

    @Override
    public boolean isPausePressed() {
        return wasPressed(bindings.get(Action.PAUSE));
    }

    /**
     * @return la configuration des touches (modifiable depuis l'écran « Touches »)
     */
    public KeyBindings getBindings() {
        return bindings;
    }

    /**
     * Utilisé par l'écran « Touches » pour capturer la touche choisie par le joueur.
     *
     * @return une touche dont l'appui commence à cette frame, ou vide s'il n'y en a aucune
     */
    public Optional<KeyCode> anyJustPressed() {
        return frameJustPressed.stream().findFirst();
    }
}