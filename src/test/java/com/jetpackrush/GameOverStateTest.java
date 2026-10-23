package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests de la fin de partie ({@link GameOverState}) : mort du joueur, rejouer et quitter. */
class GameOverStateTest {

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

    /** Tue le joueur et attend la fin de sa chute. */
    private void die() {
        play.getPlayer().die();
        game.update(PlayState.DEATH_DELAY);
    }

    private GameOverState gameOver() {
        return (GameOverState) game.getState();
    }

    @Test
    void deathShowsTheGameOverScreenAfterTheFall() {
        play.getPlayer().die();
        game.update(PlayState.DEATH_DELAY - 0.1);
        assertSame(play, game.getState());

        game.update(0.2);
        assertInstanceOf(GameOverState.class, game.getState());
        assertEquals(GameOverState.RETRY, gameOver().getSelected());
    }

    @Test
    void escapeDoesNotPauseOnceDead() {
        play.getPlayer().die();
        tap(KeyCode.ESCAPE);
        assertSame(play, game.getState());
    }

    @Test
    void retryStartsAFreshGame() {
        die();
        tap(KeyCode.ENTER);
        PlayState next = assertInstanceOf(PlayState.class, game.getState());
        assertNotSame(play, next);
        assertFalse(next.getPlayer().isDead());
    }

    @Test
    void spaceAlsoRetries() {
        die();
        tap(KeyCode.SPACE);
        assertInstanceOf(PlayState.class, game.getState());
    }

    @Test
    void quitGoesBackToTheMainMenu() {
        die();
        tap(KeyCode.DOWN);
        assertEquals(GameOverState.QUIT, gameOver().getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(MenuState.class, game.getState());
        assertFalse(quitCalled);
    }

    @Test
    void escapeGoesBackToTheMainMenu() {
        die();
        tap(KeyCode.ESCAPE);
        assertInstanceOf(MenuState.class, game.getState());
    }

    @Test
    void selectionWrapsAround() {
        die();
        tap(KeyCode.UP);
        assertEquals(GameOverState.QUIT, gameOver().getSelected());
        tap(KeyCode.DOWN);
        assertEquals(GameOverState.RETRY, gameOver().getSelected());
    }
}
