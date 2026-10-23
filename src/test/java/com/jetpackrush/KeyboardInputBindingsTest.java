package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import com.jetpackrush.KeyBindings.Action;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

/** Teste que le clavier suit bien les touches remappées, sans fenêtre JavaFX. */
class KeyboardInputBindingsTest {

    private final KeyboardInput input = new KeyboardInput();

    @Test
    void defaultKeysStillWork() {
        input.press(KeyCode.SPACE);
        assertTrue(input.isThrustPressed());
        input.release(KeyCode.SPACE);
        input.press(KeyCode.ESCAPE);
        input.poll();
        assertTrue(input.isPausePressed());
    }

    @Test
    void remappedThrustKeyReplacesTheOldOne() {
        input.getBindings().bind(Action.THRUST, KeyCode.W);
        input.press(KeyCode.SPACE);
        assertFalse(input.isThrustPressed());
        input.release(KeyCode.SPACE);
        input.press(KeyCode.W);
        assertTrue(input.isThrustPressed());
    }

    @Test
    void remappedPauseKeyReplacesTheOldOne() {
        input.getBindings().bind(Action.PAUSE, KeyCode.P);
        input.press(KeyCode.ESCAPE);
        input.poll();
        assertFalse(input.isPausePressed());
        input.release(KeyCode.ESCAPE);
        input.press(KeyCode.P);
        input.poll();
        assertTrue(input.isPausePressed());
    }

    @Test
    void lockedThrustIsReleasedWithTheRemappedKey() {
        input.getBindings().bind(Action.THRUST, KeyCode.W);
        input.press(KeyCode.W);
        input.lockThrust();
        assertFalse(input.isThrustPressed());
        input.release(KeyCode.W);
        input.press(KeyCode.W);
        assertTrue(input.isThrustPressed());
    }

    @Test
    void anyJustPressedReportsTheKeyOfTheCurrentFrameOnly() {
        assertEquals(Optional.empty(), input.anyJustPressed());
        input.press(KeyCode.Q);
        input.poll();
        assertEquals(Optional.of(KeyCode.Q), input.anyJustPressed());
        input.poll();
        assertEquals(Optional.empty(), input.anyJustPressed());
    }
}