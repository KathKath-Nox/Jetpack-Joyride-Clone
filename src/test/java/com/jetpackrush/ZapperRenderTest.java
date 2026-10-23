package com.jetpackrush;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin du Zapper avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class ZapperRenderTest {

    private static final String SHEET = "zapper_sprite_sheet.png";
    private static final double X = 100;
    private static final double Y = 200;

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final GameSpeed speed = new GameSpeed();

    private static boolean sheetLoaded() {
        return Sprites.get(SHEET) != null;
    }

    private static double expectedAngle(Zapper.Orientation orientation, double spin) {
        return switch (orientation) {
            case HORIZONTAL, HORIZONTAL_LONG -> 90;
            case VERTICAL, VERTICAL_LONG -> 0;
            case DIAGONAL -> -45;
            case ROTATING -> spin - 90;
        };
    }

    private Zapper zapper(Zapper.Orientation orientation) {
        return new Zapper(orientation, X, Y, speed);
    }

    @Test
    void everyOrientationDrawsThreeSlicesAroundItsCenter() {
        assumeTrue(sheetLoaded());
        for (Zapper.Orientation orientation : Zapper.Orientation.values()) {
            GraphicsContext g = mock(GraphicsContext.class);
            zapper(orientation).render(g);
            verify(g).translate(X + orientation.width() / 2, Y + orientation.height() / 2);
            verify(g).rotate(expectedAngle(orientation, 0));
            verify(g, times(3)).drawImage(isA(Image.class), anyDouble(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
            verify(g).restore();
        }
    }

    @Test
    void animationCyclesThroughTheFourFrames() {
        assumeTrue(sheetLoaded());
        Image sheet = Sprites.get(SHEET);
        double frameWidth = Math.floor(sheet.getWidth() / 4);
        double[] times = {0, 0.1, 0.2, 0.3, 0.4};
        int[] frames = {0, 1, 2, 3, 0};
        for (int i = 0; i < times.length; i++) {
            GraphicsContext g = mock(GraphicsContext.class);
            Zapper zapper = zapper(Zapper.Orientation.HORIZONTAL);
            if (times[i] > 0) {
                zapper.update(times[i]);
            }
            zapper.render(g);
            verify(g).drawImage(eq(sheet), eq(frames[i] * frameWidth + 28), eq(0.0), eq(54.0), eq(44.0),
                anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Test
    void rotatingZapperTurnsWithItsSpin() {
        assumeTrue(sheetLoaded());
        Zapper zapper = zapper(Zapper.Orientation.ROTATING);
        zapper.update(0.5);
        zapper.render(gc);
        verify(gc).rotate(zapper.getSpin() - 90);
    }

    @Test
    void freshZapperKeepsFullAlpha() {
        assumeTrue(sheetLoaded());
        zapper(Zapper.Orientation.VERTICAL).render(gc);
        verify(gc, never()).setGlobalAlpha(anyDouble());
    }

    @Test
    void spentZapperIsDrawnFaded() {
        assumeTrue(sheetLoaded());
        Zapper zapper = zapper(Zapper.Orientation.VERTICAL);
        zapper.onCollision(new Player(200, 300, () -> false));
        zapper.render(gc);
        verify(gc).setGlobalAlpha(0.35);
    }

    @Test
    void fallbackDrawsABarAndTwoCapsWithoutSprite() {
        assumeTrue(!sheetLoaded());
        zapper(Zapper.Orientation.HORIZONTAL).render(gc);
        verify(gc).setStroke(Color.GOLD);
        verify(gc).setLineWidth(12);
        verify(gc).strokeLine(X + 14, Y + 14, X + 160 - 14, Y + 14);
        verify(gc, times(2)).fillOval(anyDouble(), anyDouble(), eq(28.0), eq(28.0));
    }

    @Test
    void fallbackWorksForEveryOrientation() {
        assumeTrue(!sheetLoaded());
        for (Zapper.Orientation orientation : Zapper.Orientation.values()) {
            GraphicsContext g = mock(GraphicsContext.class);
            zapper(orientation).render(g);
            verify(g).strokeLine(anyDouble(), anyDouble(), anyDouble(), anyDouble());
        }
    }

    @Test
    void fallbackIsGreyWhenSpent() {
        assumeTrue(!sheetLoaded());
        Zapper zapper = zapper(Zapper.Orientation.HORIZONTAL);
        zapper.onCollision(new Player(200, 300, () -> false));
        zapper.render(gc);
        verify(gc).setStroke(Color.GRAY);
        verify(gc).setFill(Color.DARKGRAY);
    }
}
