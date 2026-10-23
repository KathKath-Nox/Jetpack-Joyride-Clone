package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

/**
 * Teste le cache d'images : une image est chargée une seule fois, et une image absente ou
 * illisible est traitée comme manquante.
 */
class SpritesCacheTest {

    @Test
    void loadedSpriteIsReturnedFromTheCache() {
        assumeTrue(Sprites.get("missile.png") != null);
        assertSame(Sprites.get("missile.png"), Sprites.get("missile.png"));
    }

    @Test
    void missingSpriteIsRememberedAsMissing() {
        assertNull(Sprites.get("does-not-exist.png"));
        assertNull(Sprites.get("does-not-exist.png"));
    }

    @Test
    void corruptedImageIsTreatedAsMissing() {
        assertNull(Sprites.get("broken.png"));
    }
}
