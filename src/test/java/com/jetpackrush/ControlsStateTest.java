package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jetpackrush.KeyBindings.Action;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Teste la logique de l'écran « Touches » (navigation, capture, conflits), sans fenêtre JavaFX. */
class ControlsStateTest {

    private KeyboardInput input;
    private KeyBindings bindings;
    private Game game;
    private ControlsState controls;

    @BeforeEach
    void setUp() {
        input = new KeyboardInput();
        bindings = input.getBindings();
        game = new Game(input, () -> { });
        game.showControls();
        controls = (ControlsState) game.getState();
    }

    // Simule une frame où la touche vient d'être appuyée puis relâchée
    private void tap(KeyCode key) {
        input.press(key);
        input.poll();
        game.getState().update(0.016);
        input.release(key);
    }

    @Test
    void opensOnTheFirstAction() {
        assertEquals(0, controls.getSelected());
        assertFalse(controls.isListening());
    }

    @Test
    void arrowsMoveTheSelectionAndWrapAround() {
        tap(KeyCode.UP);
        assertEquals(ControlsState.BACK, controls.getSelected());
        tap(KeyCode.DOWN);
        assertEquals(0, controls.getSelected());
        tap(KeyCode.DOWN);
        assertEquals(1, controls.getSelected());
    }

    @Test
    void validatingAnActionStartsListening() {
        tap(KeyCode.ENTER);
        assertTrue(controls.isListening());
    }

    @Test
    void nextKeyBecomesTheNewBinding() {
        tap(KeyCode.ENTER);
        tap(KeyCode.W);
        assertFalse(controls.isListening());
        assertEquals(KeyCode.W, bindings.get(Action.THRUST));
        assertEquals("", controls.getMessage());
    }

    @Test
    void keyAlreadyUsedIsRefusedWithAMessage() {
        tap(KeyCode.ENTER);
        tap(KeyCode.ESCAPE);
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
        assertEquals("DEJA UTILISEE PAR PAUSE", controls.getMessage());
    }

    @Test
    void reservedKeyIsRefusedWithAMessage() {
        tap(KeyCode.SPACE);
        tap(KeyCode.F11);
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
        assertEquals("TOUCHE RESERVEE", controls.getMessage());
    }

    @Test
    void escapeWhileListeningIsCapturedNotUsedToLeave() {
        tap(KeyCode.DOWN);
        tap(KeyCode.ENTER);
        tap(KeyCode.P);
        assertEquals(KeyCode.P, bindings.get(Action.PAUSE));
        assertInstanceOf(ControlsState.class, game.getState());
    }

    @Test
    void resetRestoresTheDefaultKeys() {
        bindings.bind(Action.THRUST, KeyCode.W);
        tap(KeyCode.UP);
        tap(KeyCode.UP);
        assertEquals(ControlsState.RESET, controls.getSelected());
        tap(KeyCode.ENTER);
        assertEquals(KeyCode.SPACE, bindings.get(Action.THRUST));
    }

    @Test
    void backReturnsToTheMenu() {
        tap(KeyCode.UP);
        assertEquals(ControlsState.BACK, controls.getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(MenuState.class, game.getState());
    }

    @Test
    void escapeReturnsToTheMenu() {
        tap(KeyCode.ESCAPE);
        assertInstanceOf(MenuState.class, game.getState());
    }

    @Test
    void movingClearsTheErrorMessage() {
        tap(KeyCode.ENTER);
        tap(KeyCode.ESCAPE);
        assertFalse(controls.getMessage().isEmpty());
        tap(KeyCode.DOWN);
        assertEquals("", controls.getMessage());
    }
}
