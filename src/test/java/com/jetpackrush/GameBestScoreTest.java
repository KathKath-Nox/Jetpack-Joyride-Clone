package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameBestScoreTest {

    private static Game newGame() {
        return new Game(new KeyboardInput(), () -> { });
    }

    private static PlayState playedMeters(Game game, int meters) {
        PlayState play = new PlayState(game, () -> false);
        play.getScore().addDistance(meters * ScoreManager.PIXELS_PER_METER);
        return play;
    }

    @Test
    void gameOverShowsTheGameOverScreen() {
        Game game = newGame();
        game.gameOver(playedMeters(game, 50));
        assertTrue(game.getState() instanceof GameOverState);
    }

    @Test
    void gameOverKeepsTheBestDistanceOfTheSession() {
        Game game = newGame();
        game.gameOver(playedMeters(game, 120));
        assertEquals(120, game.getBestScore().get());
        assertTrue(game.getBestScore().isNewRecord());

        game.gameOver(playedMeters(game, 80));
        assertEquals(120, game.getBestScore().get());
        assertFalse(game.getBestScore().isNewRecord());
    }
}
