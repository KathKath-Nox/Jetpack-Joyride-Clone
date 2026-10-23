package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SpritesTest {

    @Test
    void unknownSpriteIsNull() {
        assertNull(Sprites.get("does-not-exist.png"));
    }

    @Test
    void assetsArePackaged() {
        String[] names = {
            "zapper_sprite_sheet.png", "missile.png", "missile_coming.png",
            "missile_target.png", "laserfiring.png", "laserbeam.png", "laseremitter.png",
            "scientist_stationary.png", "scientist_moving.png", "scientist_crouching.png",
            "scientist_fainting.png", "scientist_fainting_upsidedown.png",
            "intro_room.png", "swamp_double.png", "junkyard_double.png"
        };
        for (String name : names) {
            assertNotNull(Sprites.class.getResource("/assets/" + name), name);
        }
    }
}