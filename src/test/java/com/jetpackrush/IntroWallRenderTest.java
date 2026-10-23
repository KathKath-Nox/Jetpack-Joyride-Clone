package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Random;

import javafx.scene.canvas.GraphicsContext;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin du mur d'entrée avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class IntroWallRenderTest {

    private static final int FRAGMENTS = IntroWall.COLUMNS * IntroWall.ROWS;

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final IntroWall wall = new IntroWall(500.5, new GameSpeed());

    @Test
    void standingWallIsDrawnFromItsRoundedLeftEdge() {
        wall.render(gc);
        verify(gc).fillRect(500.0, 0.0, IntroWall.WIDTH, IntroWall.HEIGHT);
    }

    @Test
    void standingWallHasPanelsLinesAndRivets() {
        wall.render(gc);
        verify(gc, times(7)).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(gc, times(7)).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(gc, times(14)).fillOval(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(gc).setLineWidth(3);
    }

    @Test
    void brokenWallDrawsEveryFragmentRotatedAndFullyOpaqueAtFirst() {
        wall.shatter(new Random(42));
        assertFalse(wall.isFinished());
        wall.render(gc);
        verify(gc, times(FRAGMENTS)).save();
        verify(gc, times(FRAGMENTS)).translate(anyDouble(), anyDouble());
        verify(gc, times(FRAGMENTS)).rotate(anyDouble());
        verify(gc, times(FRAGMENTS)).setGlobalAlpha(1.0);
        verify(gc, times(FRAGMENTS)).fillRect(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(gc, times(FRAGMENTS)).restore();
    }

    @Test
    void fragmentsFadeOutAsTheyAge() {
        wall.shatter(new Random(42));
        wall.update(0.8);
        wall.render(gc);
        verify(gc, times(FRAGMENTS)).setGlobalAlpha(0.5);
    }

    @Test
    void finishedWallDrawsNothing() {
        wall.shatter(new Random(42));
        wall.update(1.6);
        assertTrue(wall.isFinished());
        wall.render(gc);
        verifyNoInteractions(gc);
    }
}
