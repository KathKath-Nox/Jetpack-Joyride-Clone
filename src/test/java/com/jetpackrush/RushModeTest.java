package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Teste le mode Rush (partie accélérée pour la démo), sans fenêtre JavaFX. */
class RushModeTest {

    private static final double DT = 1.0 / 60;

    private KeyboardInput input;
    private Game game;

    @BeforeEach
    void setUp() {
        input = new KeyboardInput();
        game = new Game(input, () -> { });
    }

    // Joue l'intro jusqu'à la première frame de vraie partie, puis rend la partie
    private PlayState playPastIntro(boolean rush) {
        PlayState play = new PlayState(game, input, rush);
        for (int i = 0; i < 3000 && play.getElapsed() == 0; i++) {
            play.update(DT);
        }
        assertTrue(play.getElapsed() > 0, "l'intro devrait être terminée");
        return play;
    }

    // ---------- PlayState ----------

    @Test
    void normalGameIsNotRush() {
        assertFalse(new PlayState(game, input).isRush());
    }

    @Test
    void rushGameIsRush() {
        assertTrue(new PlayState(game, input, true).isRush());
    }

    @Test
    void rushRunsTheGameClockFasterThanNormal() {
        PlayState normal = playPastIntro(false);
        PlayState rush = playPastIntro(true);
        for (int i = 0; i < 119; i++) {
            normal.update(DT);
            rush.update(DT);
        }
        assertEquals(normal.getElapsed() * RushMode.TIME_FACTOR, rush.getElapsed(), 1e-9);
    }

    @Test
    void rushSpeedsUpFasterThanNormal() {
        PlayState normal = playPastIntro(false);
        PlayState rush = playPastIntro(true);
        for (int i = 0; i < 119; i++) {
            normal.update(DT);
            rush.update(DT);
        }
        assertTrue(rush.getScrollSpeed() > normal.getScrollSpeed());
    }

    @Test
    void rushUsesShorterDecorZones() {
        PlayState normal = new PlayState(game, input, false);
        PlayState rush = new PlayState(game, input, true);
        assertEquals(Decor.ZONE_LENGTH, normal.getDecor().getZoneLength());
        assertEquals(RushMode.ZONE_LENGTH, rush.getDecor().getZoneLength());
        assertTrue(RushMode.ZONE_LENGTH < Decor.ZONE_LENGTH);
    }

    // ---------- Decor ----------

    @Test
    void decorDefaultZoneLengthIsUnchanged() {
        assertEquals(Decor.ZONE_LENGTH, new Decor(new GameSpeed()).getZoneLength());
    }

    @Test
    void shortZonesChangeTheDecorSooner() {
        Decor normal = new Decor(new GameSpeed(1000));
        Decor shortZones = new Decor(new GameSpeed(1000), 1000);
        for (int i = 0; i < 3; i++) {
            normal.update(1.0);
            shortZones.update(1.0);
        }
        assertEquals(3000, shortZones.getDistance());
        assertEquals(0, normal.getZone());
        assertEquals(1, shortZones.getZone());
    }

    @Test
    void decorRefusesAZoneLengthThatIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () -> new Decor(new GameSpeed(), 0));
        assertThrows(IllegalArgumentException.class, () -> new Decor(new GameSpeed(), -5));
    }

    // ---------- Game ----------

    @Test
    void gameStartsInNormalMode() {
        game.startNewGame();
        assertFalse(assertInstanceOf(PlayState.class, game.getState()).isRush());
    }

    @Test
    void startRushGameStartsARushGame() {
        game.startRushGame();
        assertTrue(assertInstanceOf(PlayState.class, game.getState()).isRush());
    }

    @Test
    void replayingAfterGameOverKeepsTheRushMode() {
        game.startRushGame();
        game.gameOver((PlayState) game.getState());
        game.startNewGame();
        assertTrue(assertInstanceOf(PlayState.class, game.getState()).isRush());
    }

    @Test
    void backToTheMenuGoesBackToNormalMode() {
        game.startRushGame();
        game.backToMenu();
        game.startNewGame();
        assertFalse(assertInstanceOf(PlayState.class, game.getState()).isRush());
    }
}
