package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

/**
 * Teste le dessin des scientifiques avec un faux GraphicsContext : pas de fenêtre nécessaire.
 */
class ScientistRenderTest {

    private static final String STANDING = "scientist_stationary.png";
    private static final String MOVING = "scientist_moving.png";
    private static final String CROUCHING = "scientist_crouching.png";
    private static final String TUMBLING = "scientist_fainting_upsidedown.png";
    private static final String FAINTED = "scientist_fainting.png";

    private final GraphicsContext gc = mock(GraphicsContext.class);
    private final GameSpeed speed = new GameSpeed();

    private static void assumeAllSprites() {
        for (String name : new String[] {STANDING, MOVING, CROUCHING, TUMBLING, FAINTED}) {
            assumeTrue(Sprites.get(name) != null);
        }
    }

    private static Image sprite(String name) {
        return Sprites.get(name);
    }

    private void verifyDrawn(GraphicsContext g, String name) {
        verify(g).drawImage(eq(sprite(name)), anyDouble(), anyDouble(), anyDouble(), anyDouble());
    }

    private Scientist walker(double walkSpeed) {
        return new Scientist(300, walkSpeed, 0, speed);
    }

    private Scientist launched() {
        Scientist scientist = walker(50);
        scientist.react(new Hitbox(300, 560, 40, 60), false);
        assertTrue(scientist.isKnocked());
        return scientist;
    }

    @Test
    void walkerStartsWithTheStandingSprite() {
        assumeAllSprites();
        Scientist scientist = walker(50);
        scientist.render(gc);
        verifyDrawn(gc, STANDING);
        verify(gc).translate(scientist.getX() + Scientist.WIDTH / 2, scientist.getY() + Scientist.HEIGHT);
        verify(gc).restore();
    }

    @Test
    void walkerAlternatesToTheMovingSprite() {
        assumeAllSprites();
        Scientist scientist = walker(50);
        scientist.update(0.2);
        scientist.render(gc);
        verifyDrawn(gc, MOVING);
    }

    @Test
    void walkerGoingRightIsMirrored() {
        assumeAllSprites();
        walker(50).render(gc);
        verify(gc).scale(-1, 1);
    }

    @Test
    void walkerGoingLeftIsNotMirrored() {
        assumeAllSprites();
        walker(-50).render(gc);
        verify(gc, never()).scale(anyDouble(), anyDouble());
    }

    @Test
    void scaredScientistCrouches() {
        assumeAllSprites();
        Scientist scientist = walker(50);
        scientist.react(new Hitbox(300, 100, 40, 60), false);
        assertTrue(scientist.isScared());
        scientist.render(gc);
        verifyDrawn(gc, CROUCHING);
    }

    @Test
    void launchedScientistTumblesAroundItsCenter() {
        assumeAllSprites();
        Scientist scientist = launched();
        scientist.render(gc);
        verifyDrawn(gc, TUMBLING);
        verify(gc).translate(scientist.getX() + Scientist.WIDTH / 2, scientist.getY() + Scientist.HEIGHT / 2);
        verify(gc).rotate(0);
        verify(gc, never()).scale(anyDouble(), anyDouble());
    }

    @Test
    void faintedScientistLiesOnTheFloor() {
        assumeAllSprites();
        Scientist scientist = launched();
        for (int i = 0; i < 200 && !scientist.isFainted(); i++) {
            scientist.update(0.05);
        }
        assertTrue(scientist.isFainted());
        scientist.render(gc);
        verifyDrawn(gc, FAINTED);
    }

    @Test
    void fallbackIsABeigeRectangleWithoutSprite() {
        assumeTrue(Sprites.get(STANDING) == null);
        Scientist scientist = walker(50);
        scientist.render(gc);
        verify(gc).setFill(Color.BEIGE);
        verify(gc).fillRect(scientist.getX(), scientist.getY(), Scientist.WIDTH, Scientist.HEIGHT);
    }

    @Test
    void fallbackIsGreyWhenKnocked() {
        assumeTrue(Sprites.get(TUMBLING) == null);
        launched().render(gc);
        verify(gc).setFill(Color.GRAY);
    }
}
