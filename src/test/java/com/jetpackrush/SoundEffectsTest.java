package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SoundEffectsTest {
    private final List<String> played = new ArrayList<>();
    private final List<String> calls = new ArrayList<>();
    private final SoundPlayer recorder = new SoundPlayer() {
        @Override
        public void play(String name) {
            calls.add("play " + name);
        }

        @Override
        public void loop(String name, float gainDb) {
            calls.add("loop " + name);
        }

        @Override
        public void setGain(String name, float gainDb) {
            calls.add("gain " + name + " " + Math.round(gainDb));
        }

        @Override
        public void stop(String name) {
            calls.add("stop " + name);
        }
    };
    private final GameEvents events = new GameEvents();

    SoundEffectsTest() {
        events.addListener(new SoundEffects(recorder));
    }

    @Test
    void everyEventHasASoundFileInTheAssets() {
        for (GameEvent event : GameEvent.values()) {
            for (String sound : SoundEffects.soundsFor(event)) {
                assertNotNull(sound, event.name());
                Assets.open(sound);
            }
        }
    }

    @Test
    void eventPlaysItsSound() {
        events.publish(GameEvent.COIN_COLLECTED);
        assertEquals(List.of("play coin_pickup_1.wav"), calls);
    }

    @Test
    void removedListenerStaysSilent() {
        GameEvents other = new GameEvents();
        GameEventListener listener = new SoundEffects(recorder);
        other.addListener(listener);
        other.removeListener(listener);
        other.publish(GameEvent.COIN_COLLECTED);
        assertEquals(0, calls.size());
    }

    @Test
    void shieldAbsorbingAHitPublishesProtectionLost() {
        List<GameEvent> seen = new ArrayList<>();
        Player player = new Player(0, 0, () -> false);
        player.setEvents(new GameEvents());
        GameEvents bus = new GameEvents();
        bus.addListener(seen::add);
        player.setEvents(bus);
        player.giveShield();
        player.hit();
        player.hit();
        assertEquals(List.of(GameEvent.PROTECTION_LOST, GameEvent.PLAYER_SHOCKED), seen);
    }

    @Test
    void playerDiesOnlyOnce() {
        List<GameEvent> seen = new ArrayList<>();
        Player player = new Player(0, 0, () -> false);
        GameEvents bus = new GameEvents();
        bus.addListener(seen::add);
        player.setEvents(bus);
        player.die();
        player.die();
        assertEquals(List.of(GameEvent.PLAYER_SHOCKED), seen);
    }

    @Test
    void jetpackRumbleLoopsWhileThrustingAndStopsAfter() {
        events.publish(GameEvent.THRUST_STARTED);
        events.publish(GameEvent.THRUST_STOPPED);
        assertEquals(List.of("play jetpack_start.wav", "loop jetpack_lp.wav",
            "stop jetpack_lp.wav", "play jetpack_stop.wav"), calls);
    }

    @Test
    void musicLoopsTheTheme() {
        new SoundEffects(recorder).startMusic();
        assertEquals(List.of("loop theme.ogg"), calls);
    }

    @Test
    void settingsChangeTheMusicLive() {
        SoundSettings settings = new SoundSettings();
        SoundEffects effects = new SoundEffects(recorder, settings);
        effects.startMusic();
        calls.clear();
        settings.setMusicVolume(100);
        settings.setMusicOn(false);
        settings.setMusicOn(true);
        assertEquals(List.of("gain theme.ogg 0", "stop theme.ogg", "loop theme.ogg"), calls);
    }

    @Test
    void musicStaysSilentUntilStartedAndWhenMuted() {
        SoundSettings settings = new SoundSettings();
        settings.setMusicOn(false);
        SoundEffects effects = new SoundEffects(recorder, settings);
        settings.setMusicVolume(80);
        effects.startMusic();
        assertTrue(calls.isEmpty());
    }

    @Test
    void mutedEffectsPlayNothingButStillStopTheRumble() {
        SoundSettings settings = new SoundSettings();
        GameEvents bus = new GameEvents();
        bus.addListener(new SoundEffects(recorder, settings));
        settings.setEffectsOn(false);
        calls.clear();
        bus.publish(GameEvent.COIN_COLLECTED);
        bus.publish(GameEvent.THRUST_STARTED);
        bus.publish(GameEvent.THRUST_STOPPED);
        assertEquals(List.of("stop jetpack_lp.wav"), calls);
    }

    @Test
    void playerPublishesThrustStartAndStopOnce() {
        boolean[] thrust = {false};
        List<GameEvent> seen = new ArrayList<>();
        Player player = new Player(0, 0, () -> thrust[0]);
        GameEvents bus = new GameEvents();
        bus.addListener(seen::add);
        player.setEvents(bus);
        player.update(0.016);
        thrust[0] = true;
        player.update(0.016);
        player.update(0.016);
        thrust[0] = false;
        player.update(0.016);
        assertEquals(List.of(GameEvent.THRUST_STARTED, GameEvent.THRUST_STOPPED), seen);
    }

    @Test
    void resumeSoundsRestartsTheRumbleOnlyIfStillThrusting() {
        boolean[] thrust = {true};
        List<GameEvent> seen = new ArrayList<>();
        Player player = new Player(0, 0, () -> thrust[0]);
        GameEvents bus = new GameEvents();
        bus.addListener(seen::add);
        player.setEvents(bus);
        player.update(0.016);
        seen.clear();
        player.resumeSounds();
        assertEquals(List.of(GameEvent.THRUST_STARTED), seen);
        thrust[0] = false;
        player.update(0.016);
        seen.clear();
        player.resumeSounds();
        assertTrue(seen.isEmpty());
    }
}
