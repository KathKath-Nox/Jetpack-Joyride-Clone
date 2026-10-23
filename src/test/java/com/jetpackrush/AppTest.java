package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Tests de base de la classe {@link App}. */
class AppTest {

    @Test
    void windowSizeIs1280x720() {
        assertEquals(1280, App.WIDTH);
        assertEquals(720, App.HEIGHT);
    }
}
