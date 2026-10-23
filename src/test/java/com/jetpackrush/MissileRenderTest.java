package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin du missile avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class MissileRenderTest {

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final Missile missile = new Missile(1000, 300, new GameSpeed(), () -> null);

    private void toFlying() {
        missile.update(Missile.WARNING_TIME);
        assertTrue(missile.isFlying());
    }

    @Test
    void warningDrawsTheAlertIcon() {
        assumeTrue(Sprites.get("missile_coming.png") != null);
        missile.render(gc);
        verify(gc).drawImage(isA(Image.class), anyDouble(), eq(missile.getAlertY() - 28), eq(56.0), eq(56.0));
    }

    @Test
    void warningFallsBackToATriangleWithoutIcon() {
        assumeTrue(Sprites.get("missile_coming.png") == null);
        missile.render(gc);
        verify(gc).setFill(Color.RED);
        verify(gc).fillPolygon(any(), any(), eq(3));
        verify(gc).fillText(eq("!"), anyDouble(), anyDouble());
    }

    @Test
    void lockedAlertBlinksOffOnEvenFrames() {
        missile.update(1.0);
        assertTrue(missile.isWarning());
        missile.render(gc);
        verifyNoInteractions(gc);
    }

    @Test
    void lockedAlertShowsTheTargetIconOnOddFrames() {
        assumeTrue(Sprites.get("missile_target.png") != null);
        missile.update(1.1);
        assertTrue(missile.isWarning());
        missile.render(gc);
        verify(gc).drawImage(isA(Image.class), anyDouble(), anyDouble(), eq(56.0), eq(56.0));
    }

    @Test
    void flyingDrawsTheBodyThenResetsTheAlpha() {
        assumeTrue(Sprites.get("missile.png") != null);
        toFlying();
        missile.render(gc);
        verify(gc).drawImage(isA(Image.class), anyDouble(), anyDouble(), anyDouble(), anyDouble());
        verify(gc).setGlobalAlpha(1);
    }

    @Test
    void flyingFallsBackToAShapeWithoutSprite() {
        assumeTrue(Sprites.get("missile.png") == null);
        toFlying();
        missile.render(gc);
        verify(gc).setFill(Color.DARKRED);
        verify(gc).fillPolygon(any(), any(), eq(3));
    }

    @Test
    void spentMissileIsDrawnFadedWithSprite() {
        assumeTrue(Sprites.get("missile.png") != null);
        toFlying();
        missile.onCollision(new Player(200, 300, () -> false));
        missile.render(gc);
        verify(gc).setGlobalAlpha(0.35);
        verify(gc).setGlobalAlpha(1);
    }

    @Test
    void spentMissileUsesGreyColorsWithoutSprite() {
        assumeTrue(Sprites.get("missile.png") == null);
        toFlying();
        missile.onCollision(new Player(200, 300, () -> false));
        missile.render(gc);
        verify(gc).setFill(Color.DARKGRAY);
    }
}
