package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BestScoreTest {

    @Test
    void startsAtZeroWithoutRecord() {
        BestScore best = new BestScore();
        assertEquals(0, best.get());
        assertFalse(best.isNewRecord());
    }

    @Test
    void higherDistanceBecomesTheRecord() {
        BestScore best = new BestScore();
        best.submit(120);
        assertEquals(120, best.get());
        assertTrue(best.isNewRecord());
    }

    @Test
    void lowerDistanceKeepsTheBest() {
        BestScore best = new BestScore();
        best.submit(120);
        best.submit(80);
        assertEquals(120, best.get());
        assertFalse(best.isNewRecord());
    }

    @Test
    void sameDistanceIsNotARecord() {
        BestScore best = new BestScore();
        best.submit(120);
        best.submit(120);
        assertEquals(120, best.get());
        assertFalse(best.isNewRecord());
    }

    @Test
    void zeroDistanceIsNotARecord() {
        BestScore best = new BestScore();
        best.submit(0);
        assertEquals(0, best.get());
        assertFalse(best.isNewRecord());
    }
}
