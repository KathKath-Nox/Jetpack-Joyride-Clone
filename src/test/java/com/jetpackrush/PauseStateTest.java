package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la pause ({@link PauseState}) : entrée, compte à rebours et sortie. */
class PauseStateTest {

    private KeyboardInput input;
    private Game game;
    private boolean quitCalled;
    private PlayState play;

    @BeforeEach
    void setUp() {
        input = new KeyboardInput();
        game = new Game(input, () -> quitCalled = true);
        tap(KeyCode.ENTER); // « Jouer » dans le menu principal
        play = (PlayState) game.getState();
    }

    /** Appuie puis relâche une touche, avec une frame entre les deux. */
    private void tap(KeyCode key) {
        input.press(key);
        game.update(0);
        input.release(key);
    }

    private PauseState pause() {
        return (PauseState) game.getState();
    }

    @Test
    void escapeDuringTheGamePauses() {
        tap(KeyCode.ESCAPE);
        assertInstanceOf(PauseState.class, game.getState());
        assertEquals(PauseState.RESUME, pause().getSelected());
        assertFalse(pause().isCountingDown());
    }

    @Test
    void resumeWaitsForTheCountdownBeforeGoingBackToTheSameGame() {
        tap(KeyCode.ESCAPE);
        tap(KeyCode.ENTER);
        assertTrue(pause().isCountingDown());

        game.update(PauseState.COUNTDOWN_DURATION - 0.1);
        assertInstanceOf(PauseState.class, game.getState());

        game.update(0.2);
        assertSame(play, game.getState());
    }

    @Test
    void escapeInThePauseMenuAlsoStartsTheCountdown() {
        tap(KeyCode.ESCAPE);
        tap(KeyCode.ESCAPE);
        assertTrue(pause().isCountingDown());
    }

    @Test
    void escapeDuringTheCountdownGoesBackToThePauseMenu() {
        tap(KeyCode.ESCAPE);
        tap(KeyCode.ENTER);
        game.update(1);
        tap(KeyCode.ESCAPE);
        assertFalse(pause().isCountingDown());

        // Le compte à rebours repart de zéro au prochain « Reprendre »
        tap(KeyCode.ENTER);
        game.update(PauseState.COUNTDOWN_DURATION - 0.1);
        assertInstanceOf(PauseState.class, game.getState());
    }

    @Test
    void quitGoesBackToTheMainMenu() {
        tap(KeyCode.ESCAPE);
        tap(KeyCode.DOWN);
        assertEquals(PauseState.QUIT, pause().getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(MenuState.class, game.getState());
        assertFalse(quitCalled);
    }

    @Test
    void selectionWrapsAround() {
        tap(KeyCode.ESCAPE);
        tap(KeyCode.UP);
        assertEquals(PauseState.QUIT, pause().getSelected());
        tap(KeyCode.DOWN);
        assertEquals(PauseState.RESUME, pause().getSelected());
    }
}
