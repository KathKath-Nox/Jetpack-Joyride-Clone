package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests du menu principal ({@link MenuState}) et de ses transitions. */
class MenuStateTest {

    private KeyboardInput input;
    private Game game;
    private boolean quitCalled;

    @BeforeEach
    void setUp() {
        input = new KeyboardInput();
        game = new Game(input, () -> quitCalled = true);
    }

    /** Appuie puis relâche une touche, avec une frame entre les deux. */
    private void tap(KeyCode key) {
        input.press(key);
        game.update(0);
        input.release(key);
    }

    private MenuState menu() {
        return (MenuState) game.getState();
    }

    /** Sélectionne l'option « Customiser » (avant-dernière : on remonte de deux crans depuis JOUER). */
    private void selectSkin() {
        tap(KeyCode.UP);
        tap(KeyCode.UP);
        assertEquals(MenuState.SKIN, menu().getSelected());
    }

    @Test
    void gameStartsOnTheMenu() {
        assertInstanceOf(MenuState.class, game.getState());
        assertEquals(MenuState.PLAY, menu().getSelected());
    }

    @Test
    void enterOnPlayStartsTheGame() {
        tap(KeyCode.ENTER);
        assertInstanceOf(PlayState.class, game.getState());
        assertFalse(quitCalled);
    }

    @Test
    void spaceOnPlayStartsTheGame() {
        tap(KeyCode.SPACE);
        assertInstanceOf(PlayState.class, game.getState());
    }

    @Test
    void enterOnQuitClosesTheGame() {
        tap(KeyCode.DOWN);
        tap(KeyCode.ENTER);
        assertTrue(quitCalled);
    }

    @Test
    void escapeClosesTheGame() {
        tap(KeyCode.ESCAPE);
        assertTrue(quitCalled);
    }

    @Test
    void selectionWrapsAround() {
        tap(KeyCode.UP);
        assertEquals(MenuState.RUSH, menu().getSelected());   // MODIFIÉ : le dernier est maintenant Rush
        tap(KeyCode.DOWN);
        assertEquals(MenuState.PLAY, menu().getSelected());
    }

    @Test
    void leftAndRightAlsoMoveTheSelection() {
        tap(KeyCode.RIGHT);
        assertEquals(MenuState.QUIT, menu().getSelected());
        tap(KeyCode.LEFT);
        assertEquals(MenuState.PLAY, menu().getSelected());
    }

    @Test
    void holdingAKeyMovesTheSelectionOnlyOnce() {
        input.press(KeyCode.DOWN);
        game.update(0);
        input.press(KeyCode.DOWN); // répétition automatique du clavier
        game.update(0);
        assertEquals(MenuState.QUIT, menu().getSelected());
    }

    // AJOUTÉ : le bouton Réglages ouvre l'écran des touches
    @Test
    void enterOnSettingsOpensTheSettingsScreen() {
        tap(KeyCode.UP);
        tap(KeyCode.UP);
        tap(KeyCode.UP);
        assertEquals(MenuState.SETTINGS, menu().getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(ControlsState.class, game.getState());
        assertFalse(quitCalled);
    }

    @Test
    void enterOnCustomizeOpensTheCustomizeScreen() {
        selectSkin();
        tap(KeyCode.ENTER);
        assertInstanceOf(CustomizeState.class, game.getState());
    }

    @Test
    void rightOnCustomizeScreenSelectsTheNextSkin() {
        selectSkin();
        tap(KeyCode.ENTER);
        tap(KeyCode.RIGHT);
        assertEquals("ROUGE", game.getCharacterChoice().getName());
        assertInstanceOf(CustomizeState.class, game.getState());
    }

    @Test
    void leftOnCustomizeScreenWrapsToTheLastSkin() {
        selectSkin();
        tap(KeyCode.ENTER);
        tap(KeyCode.LEFT);
        assertEquals("ROI", game.getCharacterChoice().getName());
    }

    @Test
    void escapeOnCustomizeScreenKeepsTheSkinAndReturnsToTheMenu() {
        selectSkin();
        tap(KeyCode.ENTER);
        tap(KeyCode.RIGHT);
        tap(KeyCode.ESCAPE);
        assertInstanceOf(MenuState.class, game.getState());
        assertEquals("ROUGE", game.getCharacterChoice().getName());
        assertFalse(quitCalled);
    }

    // AJOUTÉ : le bouton Rush lance une partie en mode Rush
    @Test
    void enterOnRushStartsARushGame() {
        tap(KeyCode.UP);
        assertEquals(MenuState.RUSH, menu().getSelected());
        tap(KeyCode.ENTER);
        PlayState play = assertInstanceOf(PlayState.class, game.getState());
        assertTrue(play.isRush());
        assertFalse(quitCalled);
    }
}