package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin du laser avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class LaserRenderTest {

    private static final double Y = 300;
    private static final double CENTER_Y = Y + Laser.THICKNESS / 2;

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final Laser laser = new Laser(Y, new GameSpeed());

    private void toActive() {
        laser.update(Laser.WARNING_TIME);
        assertTrue(laser.isActive());
    }

    @Test
    void warningDrawsTheRedLine() {
        laser.render(gc);
        verify(gc).strokeLine(0, CENTER_Y, App.WIDTH, CENTER_Y);
        verify(gc).setLineWidth(3);
        verify(gc).setGlobalAlpha(1);
    }

    @Test
    void warningBlinksDim() {
        laser.render(gc);
        verify(gc).setGlobalAlpha(0.35);
    }

    @Test
    void warningBlinksBright() {
        laser.update(0.2);
        laser.render(gc);
        verify(gc).setGlobalAlpha(0.95);
    }

    @Test
    void warningGetsThickerWhenUrgent() {
        laser.update(0.9);
        assertTrue(laser.isWarning());
        laser.render(gc);
        verify(gc).setLineWidth(6);
    }

    @Test
    void warningDrawsBothEmittersWhenSpriteIsLoaded() {
        assumeTrue(Sprites.get("laseremitter.png") != null);
        laser.render(gc);
        verify(gc, times(2)).drawImage(isA(Image.class), eq(0.0), eq(0.0), anyDouble(), anyDouble());
        verify(gc).scale(-1, 1);
    }

    @Test
    void activeResetsTheAlphaAtTheEnd() {
        toActive();
        laser.render(gc);
        verify(gc).setGlobalAlpha(1);
    }

    @Test
    void activeDrawsTheBeamWithSprites() {
        assumeTrue(Sprites.get("laserbeam.png") != null);
        toActive();
        laser.render(gc);
        verify(gc).clip();
        verify(gc, atLeastOnce()).drawImage(isA(Image.class), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void activeDrawsTheMuzzleAndEmitterWithSprites() {
        assumeTrue(Sprites.get("laserfiring.png") != null && Sprites.get("laseremitter.png") != null);
        toActive();
        laser.render(gc);
        verify(gc, times(2)).drawImage(isA(Image.class), eq(0.0), anyDouble(), anyDouble(), anyDouble());
        verify(gc).scale(-1, 1);
    }

    @Test
    void activeFallsBackToAGradientWithoutBeamSprite() {
        assumeTrue(Sprites.get("laserbeam.png") == null);
        toActive();
        laser.render(gc);
        verify(gc).fillRect(anyDouble(), eq(Y), anyDouble(), eq(Laser.THICKNESS));
    }

    @Test
    void spentLaserIsDrawnFaded() {
        toActive();
        laser.onCollision(new Player(200, 300, () -> false));
        laser.render(gc);
        verify(gc).setGlobalAlpha(0.45);
        verify(gc).setGlobalAlpha(1);
    }

    @Test
    void doneLaserDrawsNothing() {
        toActive();
        laser.update(Laser.ACTIVE_TIME);
        assertTrue(laser.isDone());
        laser.render(gc);
        verifyNoInteractions(gc);
    }
}
