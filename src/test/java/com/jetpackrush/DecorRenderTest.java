package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin du décor avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class DecorRenderTest {

    private static final double SPEED = 500;

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final GameSpeed speed = new GameSpeed();
    private final Decor decor = new Decor(speed);

    /** Fait avancer le décor jusqu'à cette distance, puis le dessine. */
    private void renderAt(double distance) {
        if (distance > 0) {
            speed.setSpeed(SPEED);
            decor.update(distance / SPEED);
            assertEquals(distance, decor.getDistance(), 1e-6);
        }
        decor.render(gc);
    }

    private static boolean loaded(String name) {
        return Sprites.get(name) != null;
    }

    @Test
    void startsWithABlackBackground() {
        renderAt(0);
        verify(gc).setFill(Color.BLACK);
        verify(gc).fillRect(0, 0, App.WIDTH, App.HEIGHT);
    }

    @Test
    void startDrawsTheEntryCorridorAndTheRoom() {
        assumeTrue(loaded("background2.png") && loaded("intro_room.png"));
        renderAt(0);
        verify(gc, atLeastOnce()).clip();
        verify(gc).drawImage(Sprites.get("intro_room.png"), Decor.ENTRY_X, 0);
    }

    @Test
    void roomFallsBackToARectangleWithoutImage() {
        assumeTrue(!loaded("intro_room.png"));
        renderAt(0);
        verify(gc).fillRect(Decor.ENTRY_X, 0, Decor.ROOM_WIDTH, App.HEIGHT - Decor.FLOOR_HEIGHT);
    }

    @Test
    void roomIsNotDrawnOnceItIsBehind() {
        assumeTrue(loaded("intro_room.png"));
        renderAt(Decor.ROOM_END + 10);
        verify(gc, never()).drawImage(eq(Sprites.get("intro_room.png")), anyDouble(), anyDouble());
    }

    @Test
    void metalCorridorAlternatesMirroredTiles() {
        assumeTrue(loaded("background2.png"));
        renderAt(5000);
        verify(gc, atLeastOnce()).scale(-1, 1);
    }

    @Test
    void fadeBetweenTwoZonesUsesSeveralAlphaSteps() {
        renderAt(Decor.CORRIDOR_START + Decor.ZONE_LENGTH - 50);
        verify(gc, atLeast(4)).setGlobalAlpha(anyDouble());
    }

    @Test
    void labTilesAreMirroredToHideTheSeam() {
        assumeTrue(loaded("background.png"));
        renderAt(Decor.CORRIDOR_START + Decor.ZONE_LENGTH + 4000);
        verify(gc, atLeastOnce()).scale(-1, 1);
        verify(gc, atLeastOnce()).drawImage(isA(Image.class), anyDouble(), anyDouble(), anyDouble(),
            anyDouble(), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    @Test
    void swampIsDrawnAsTiles() {
        assumeTrue(loaded("swamp_double.png"));
        renderAt(Decor.CORRIDOR_START + 2 * Decor.ZONE_LENGTH + 3000);
        verify(gc, atLeastOnce()).drawImage(isA(Image.class), eq(0.0), eq(0.0));
    }

    @Test
    void junkyardIsDrawnAsTiles() {
        assumeTrue(loaded("junkyard_double.png"));
        renderAt(Decor.CORRIDOR_START + 3 * Decor.ZONE_LENGTH + 3000);
        verify(gc, atLeastOnce()).drawImage(isA(Image.class), eq(0.0), eq(0.0));
    }

    @Test
    void zonesWrapAroundAndMetalComesBack() {
        assumeTrue(loaded("background2.png"));
        renderAt(Decor.CORRIDOR_START + 4 * Decor.ZONE_LENGTH + 5000);
        verify(gc, atLeastOnce()).scale(-1, 1);
    }

    @Test
    void stripesAreDrawnWhenTheImageIsMissing() {
        assumeTrue(!loaded("background2.png"));
        renderAt(5000);
        verify(gc, atLeastOnce()).fillRect(anyDouble(), eq(0.0), eq(Decor.STRIPE_WIDTH), anyDouble());
    }

    @Test
    void drawingTwiceInARowIsSafe() {
        renderAt(0);
        decor.render(gc);
        verify(gc, atLeast(2)).setFill(Color.BLACK);
    }
}
