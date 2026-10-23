package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.jetpackrush.KeyBindings.Action;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

/** Teste la logique de remapping des touches, sans JavaFX. */
class KeyBindingsTest {

    private final KeyBindings bindings = new KeyBindings();

    @Test
    void startsWithTheDefaultKeys() {
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
        assertEquals(KeyCode.ESCAPE, bindings.get(Action.PAUSE));
    }

    @Test
    void freeKeyCanBeAssigned() {
        assertTrue(bindings.bind(Action.THRUST, KeyCode.W));
        assertEquals(KeyCode.W, bindings.get(Action.THRUST));
        assertTrue(bindings.matches(Action.THRUST, KeyCode.W));
        assertFalse(bindings.matches(Action.THRUST, KeyCode.SPACE));
    }

    @Test
    void keyUsedByAnotherActionIsRefused() {
        assertFalse(bindings.bind(Action.THRUST, KeyCode.ESCAPE));
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
        assertEquals(KeyCode.ESCAPE, bindings.get(Action.PAUSE));
    }

    @Test
    void conflictNamesTheActionThatOwnsTheKey() {
        assertEquals(Optional.of(Action.PAUSE), bindings.conflictWith(Action.THRUST, KeyCode.ESCAPE));
        assertEquals(Optional.empty(), bindings.conflictWith(Action.THRUST, KeyCode.W));
    }

    @Test
    void anActionIsNeverInConflictWithItself() {
        assertEquals(Optional.empty(), bindings.conflictWith(Action.THRUST, KeyCode.SPACE));
        assertTrue(bindings.bind(Action.THRUST, KeyCode.SPACE));
    }

    @Test
    void reservedKeysCannotBeAssigned() {
        for (KeyCode key : new KeyCode[] {KeyCode.ENTER, KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.F11}) {
            assertTrue(bindings.isReserved(key));
            assertFalse(bindings.bind(Action.THRUST, key));
        }
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
    }

    @Test
    void invalidArgumentsAreRefused() {
        assertFalse(bindings.bind(null, KeyCode.W));
        assertFalse(bindings.bind(Action.THRUST, null));
    }

    @Test
    void actionForFindsTheOwnerOfAKey() {
        assertEquals(Optional.of(Action.THRUST), bindings.actionFor(KeyCode.SPACE));
        assertEquals(Optional.empty(), bindings.actionFor(KeyCode.W));
        bindings.bind(Action.PAUSE, KeyCode.P);
        assertEquals(Optional.of(Action.PAUSE), bindings.actionFor(KeyCode.P));
        assertEquals(Optional.empty(), bindings.actionFor(KeyCode.ESCAPE));
    }

    @Test
    void releasedKeyBecomesAvailableForAnotherAction() {
        assertTrue(bindings.bind(Action.PAUSE, KeyCode.P));
        assertTrue(bindings.bind(Action.THRUST, KeyCode.ESCAPE));
    }

    @Test
    void resetRestoresTheDefaults() {
        bindings.bind(Action.THRUST, KeyCode.W);
        bindings.bind(Action.PAUSE, KeyCode.P);
        bindings.reset();
        for (Action action : Action.values()) {
            assertEquals(action.getDefaultKey(), bindings.get(action));
        }
    }

    @Test
    void everyActionHasALabelAndADefaultKey() {
        for (Action action : Action.values()) {
            assertFalse(action.getLabel().isBlank());
            assertFalse(bindings.isReserved(action.getDefaultKey()));
        }
    }
}
