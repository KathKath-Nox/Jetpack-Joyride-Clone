package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScoreManagerTest {

    @Test
    void startsAtZero() {
        ScoreManager score = new ScoreManager();
        assertEquals(0, score.getDistance());
        assertEquals(0, score.getCoins());
    }

    @Test
    void distanceIsConvertedToMeters() {
        ScoreManager score = new ScoreManager();
        score.addDistance(ScoreManager.PIXELS_PER_METER * 3.5);
        assertEquals(3, score.getDistance());
    }

    @Test
    void listenerReceivesCurrentStateWhenAdded() {
        ScoreManager score = new ScoreManager();
        score.addCoin();
        List<String> seen = new ArrayList<>();
        score.addListener((d, c) -> seen.add(d + "/" + c));
        assertEquals(List.of("0/1"), seen);
    }

    @Test
    void listenerIsNotifiedOnCoinAndOnWholeMeterOnly() {
        ScoreManager score = new ScoreManager();
        List<String> seen = new ArrayList<>();
        score.addListener((d, c) -> seen.add(d + "/" + c));
        seen.clear();
        score.addDistance(ScoreManager.PIXELS_PER_METER / 2);
        assertEquals(0, seen.size());
        score.addDistance(ScoreManager.PIXELS_PER_METER / 2);
        score.addCoin();
        assertEquals(List.of("1/0", "1/1"), seen);
    }

    @Test
    void removedListenerIsNoLongerNotified() {
        ScoreManager score = new ScoreManager();
        List<Integer> seen = new ArrayList<>();
        ScoreListener listener = (d, c) -> seen.add(c);
        score.addListener(listener);
        score.removeListener(listener);
        seen.clear();
        score.addCoin();
        assertEquals(0, seen.size());
    }
}
