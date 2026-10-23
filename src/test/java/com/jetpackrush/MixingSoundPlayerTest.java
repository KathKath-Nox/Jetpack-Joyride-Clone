package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jetpackrush.MixingSoundPlayer.Voice;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.Test;

class MixingSoundPlayerTest {

    private static short[] block(int frames) {
        return new short[frames * 2];
    }

    private static List<Voice> voices(Voice... all) {
        return new CopyOnWriteArrayList<>(all);
    }

    @Test
    void oneShotPlaysOnceThenIsRemoved() {
        Voice v = new Voice("a", new short[] {100, 100, 200, 200}, false, 1f);
        List<Voice> list = voices(v);
        short[] out = block(4);
        MixingSoundPlayer.mix(out, list);
        assertArrayEquals(new short[] {100, 100, 200, 200, 0, 0, 0, 0}, out);
        assertTrue(list.isEmpty());
    }

    @Test
    void loopRestartsAtTheBeginning() {
        Voice v = new Voice("a", new short[] {1, 1, 2, 2}, true, 1f);
        List<Voice> list = voices(v);
        short[] out = block(3);
        MixingSoundPlayer.mix(out, list);
        assertArrayEquals(new short[] {1, 1, 2, 2, 1, 1}, out);
        assertEquals(1, list.size());
    }

    @Test
    void voicesAreAddedTogetherAndClamped() {
        Voice a = new Voice("a", new short[] {20000, 20000}, false, 1f);
        Voice b = new Voice("b", new short[] {20000, -20000}, false, 1f);
        short[] out = block(1);
        MixingSoundPlayer.mix(out, voices(a, b));
        assertEquals(Short.MAX_VALUE, out[0]);
        assertEquals(0, out[1]);
    }

    @Test
    void gainScalesAVoice() {
        Voice v = new Voice("a", new short[] {1000, 1000}, true, MixingSoundPlayer.toLinear(-6f));
        short[] out = block(1);
        MixingSoundPlayer.mix(out, voices(v));
        assertEquals(501, out[0], 2);
    }

    @Test
    void stoppedVoiceIsDroppedSilently() {
        Voice v = new Voice("a", new short[] {500, 500}, true, 1f);
        v.stopped = true;
        List<Voice> list = voices(v);
        short[] out = block(2);
        MixingSoundPlayer.mix(out, list);
        assertArrayEquals(new short[] {0, 0, 0, 0}, out);
        assertTrue(list.isEmpty());
    }

    @Test
    void monoIsDuplicatedToBothChannelsAndResampled() {
        short[] mono = new short[320];
        for (int i = 0; i < mono.length; i++) {
            mono[i] = (short) i;
        }
        short[] stereo = MixingSoundPlayer.toMixFormat(mono, 1, 32000f);
        assertEquals(Math.round(320 * 44100.0 / 32000) * 2, stereo.length);
        assertEquals(stereo[0], stereo[1]);
        assertEquals(stereo[100], stereo[101]);
    }

    @Test
    void sameRateStereoIsKept() {
        short[] in = {1, 2, 3, 4, 5, 6};
        assertArrayEquals(in, MixingSoundPlayer.toMixFormat(in, 2, 44100f));
    }

    @Test
    void quietSoundIsBoostedAndBoostIsCapped() {
        short[] quiet = {3000, -6000, 0};
        MixingSoundPlayer.normalize(quiet);
        assertEquals(15564, quiet[0], 2);
        assertEquals(-31128, quiet[1], 2);
        short[] breath = {10, -5};
        MixingSoundPlayer.normalize(breath);
        assertEquals(80, breath[0]);
        short[] silence = {0, 0};
        MixingSoundPlayer.normalize(silence);
        assertArrayEquals(new short[] {0, 0}, silence);
    }
}
