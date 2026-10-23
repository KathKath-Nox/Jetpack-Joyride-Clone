package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HitboxTest {
    @Test
    void overlappingBoxesIntersect() {
        assertTrue(new Hitbox(0, 0, 10, 10).intersects(new Hitbox(5, 5, 10, 10)));
    }

    @Test
    void distantBoxesDoNotIntersect() {
        assertFalse(new Hitbox(0, 0, 10, 10).intersects(new Hitbox(50, 50, 10, 10)));
    }

    @Test
    void boxesThatOnlyTouchDoNotIntersect() {
        assertFalse(new Hitbox(0, 0, 10, 10).intersects(new Hitbox(10, 0, 10, 10)));
    }

    @Test
    void gettersReturnConstructorValues() {
        Hitbox box = new Hitbox(1, 2, 3, 4);
        assertEquals(1, box.getX());
        assertEquals(2, box.getY());
        assertEquals(3, box.getWidth());
        assertEquals(4, box.getHeight());
    }
}
