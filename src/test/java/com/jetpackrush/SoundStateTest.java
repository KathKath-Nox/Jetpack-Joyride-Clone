package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SoundStateTest {

    private KeyboardInput input;
    private Game game;
    private SoundSettings settings;

    @BeforeEach
    void setUp() {
        input = new KeyboardInput();
        game = new Game(input, () -> { });
        settings = game.getSoundSettings();
        game.showSound();
    }

    private void tap(KeyCode key) {
        input.press(key);
        input.poll();
        game.getState().update(0.016);
        input.release(key);
    }

    private SoundState sound() {
        return (SoundState) game.getState();
    }

    @Test
    void settingsScreenLeadsToTheSoundScreen() {
        game.showControls();
        tap(KeyCode.DOWN);
        tap(KeyCode.DOWN);
        assertEquals(ControlsState.SOUND, ((ControlsState) game.getState()).getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(SoundState.class, game.getState());
    }

    @Test
    void enterTogglesMusic() {
        assertEquals(SoundState.MUSIC_ON, sound().getSelected());
        tap(KeyCode.ENTER);
        assertFalse(settings.isMusicOn());
        tap(KeyCode.ENTER);
        assertTrue(settings.isMusicOn());
    }

    @Test
    void arrowsAdjustTheVolumeOnTheVolumeRow() {
        tap(KeyCode.DOWN);
        assertEquals(SoundState.MUSIC_VOLUME, sound().getSelected());
        tap(KeyCode.RIGHT);
        assertEquals(SoundSettings.DEFAULT_VOLUME + SoundSettings.STEP, settings.getMusicVolume());
        tap(KeyCode.LEFT);
        tap(KeyCode.LEFT);
        assertEquals(SoundSettings.DEFAULT_VOLUME - SoundSettings.STEP, settings.getMusicVolume());
    }

    @Test
    void effectsRowTogglesTheEffects() {
        tap(KeyCode.DOWN);
        tap(KeyCode.DOWN);
        assertEquals(SoundState.EFFECTS_ON, sound().getSelected());
        tap(KeyCode.ENTER);
        assertFalse(settings.isEffectsOn());
        assertTrue(settings.isMusicOn());
    }

    @Test
    void backAndEscapeReturnToTheMenu() {
        tap(KeyCode.UP);
        assertEquals(SoundState.BACK, sound().getSelected());
        tap(KeyCode.ENTER);
        assertInstanceOf(MenuState.class, game.getState());
        game.showSound();
        tap(KeyCode.ESCAPE);
        assertInstanceOf(MenuState.class, game.getState());
    }
}
