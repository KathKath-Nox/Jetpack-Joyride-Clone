package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

/** Tests du clavier ({@link KeyboardInput}) : blocage du jetpack et relâchement des touches. */
class KeyboardInputTest {

    @Test
    void spaceThatStartsTheGameDoesNotThrust() {
        KeyboardInput input = new KeyboardInput();
        Game game = new Game(input, () -> { });
        input.press(KeyCode.SPACE);
        game.update(0);

        assertInstanceOf(PlayState.class, game.getState());
        assertFalse(input.isThrustPressed());
    }

    @Test
    void newPressAfterReleaseThrusts() {
        KeyboardInput input = new KeyboardInput();
        input.press(KeyCode.SPACE);
        input.lockThrust();
        input.release(KeyCode.SPACE);
        input.press(KeyCode.SPACE);

        assertTrue(input.isThrustPressed());
    }

    @Test
    void lockWithoutSpaceHeldDoesNothing() {
        KeyboardInput input = new KeyboardInput();
        input.lockThrust();
        input.press(KeyCode.SPACE);

        assertTrue(input.isThrustPressed());
    }

    @Test
    void releaseAllReleasesHeldKeys() {
        KeyboardInput input = new KeyboardInput();
        input.press(KeyCode.SPACE);
        input.releaseAll();

        assertFalse(input.isThrustPressed());
    }
}
