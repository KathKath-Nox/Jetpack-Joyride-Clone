package com.jetpackrush;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.scene.input.KeyCode;

/**
 * Configuration des touches du jeu : associe chaque {@link Action} à une touche.
 *
 * <p>Cette classe ne dépend que de l'énumération {@link KeyCode} : elle se teste sans fenêtre
 * JavaFX. Elle gère les conflits (une touche ne peut servir qu'à une seule action), les touches
 * réservées aux menus et la remise à zéro des réglages par défaut.
 */
public class KeyBindings {

    /** Actions dont la touche peut être changée par le joueur. */
    public enum Action {
        /** Poussée du jetpack (ou saut dans un véhicule). */
        THRUST("Jetpack", KeyCode.SPACE),
        /** Pause / reprise en jeu. */
        PAUSE("Pause", KeyCode.ESCAPE);

        private final String label;
        private final KeyCode defaultKey;

        Action(String label, KeyCode defaultKey) {
            this.label = label;
            this.defaultKey = defaultKey;
        }

        /** @return le nom affiché dans l'écran « Contrôles » */
        public String getLabel() {
            return label;
        }

        /** @return la touche par défaut de cette action */
        public KeyCode getDefaultKey() {
            return defaultKey;
        }
    }

    /** Touches utilisées par les menus ou le plein écran : elles ne peuvent pas être réassignées. */
    private static final Set<KeyCode> RESERVED = EnumSet.of(
            KeyCode.ENTER, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.F11);

    private final Map<Action, KeyCode> keys = new EnumMap<>(Action.class);

    /** Crée une configuration avec les touches par défaut. */
    public KeyBindings() {
        reset();
    }

    /**
     * @param action l'action voulue
     * @return la touche actuellement associée à cette action
     */
    public KeyCode get(Action action) {
        return keys.get(action);
    }

    /**
     * @param key une touche
     * @return true si cette touche est réservée (menus, plein écran) et donc non réassignable
     */
    public boolean isReserved(KeyCode key) {
        return RESERVED.contains(key);
    }

    /**
     * Cherche l'autre action qui utilise déjà une touche.
     *
     * @param action l'action que l'on veut modifier (elle-même n'est jamais un conflit)
     * @param key la touche envisagée
     * @return l'action en conflit, ou vide si la touche est libre
     */
    public Optional<Action> conflictWith(Action action, KeyCode key) {
        for (Map.Entry<Action, KeyCode> entry : keys.entrySet()) {
            if (entry.getKey() != action && entry.getValue() == key) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * Associe une touche à une action, sauf si la touche est réservée ou déjà prise.
     * En cas d'échec, la configuration ne change pas.
     *
     * @param action l'action à modifier
     * @param key la nouvelle touche
     * @return true si la touche a été assignée, false si elle est refusée
     */
    public boolean bind(Action action, KeyCode key) {
        if (action == null || key == null || isReserved(key) || conflictWith(action, key).isPresent()) {
            return false;
        }
        keys.put(action, key);
        return true;
    }

    /**
     * @param action l'action testée
     * @param key la touche pressée
     * @return true si cette touche déclenche cette action
     */
    public boolean matches(Action action, KeyCode key) {
        return keys.get(action) == key;
    }

    /**
     * @param key la touche pressée
     * @return l'action associée à cette touche, ou vide si aucune
     */
    public Optional<Action> actionFor(KeyCode key) {
        for (Map.Entry<Action, KeyCode> entry : keys.entrySet()) {
            if (entry.getValue() == key) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /** Remet toutes les touches à leur valeur par défaut. */
    public void reset() {
        for (Action action : Action.values()) {
            keys.put(action, action.getDefaultKey());
        }
    }
}
