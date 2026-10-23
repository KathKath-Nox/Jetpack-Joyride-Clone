package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PhoneInputTest {
    private long now;
    private final PhoneInput phone = new PhoneInput(() -> now);

    @Test
    void thrustFollowsDownAndUp() {
        assertFalse(phone.isThrustPressed());
        phone.handle("1:down");
        assertTrue(phone.isThrustPressed());
        phone.handle("2:up");
        assertFalse(phone.isThrustPressed());
    }

    @Test
    void thrustStopsWhenHeartbeatsStop() {
        phone.handle("1:down");
        now += PhoneInput.HOLD_TIMEOUT_MS - 1;
        assertTrue(phone.isThrustPressed());
        now += 2;
        assertFalse(phone.isThrustPressed());
    }

    @Test
    void lateMessageIsIgnored() {
        phone.handle("5:up");
        phone.handle("4:down");
        assertFalse(phone.isThrustPressed());
    }

    @Test
    void malformedMessagesAreIgnored() {
        phone.handle("down");
        phone.handle("x:down");
        phone.handle("");
        assertFalse(phone.isThrustPressed());
    }

    @Test
    void pauseIsReadOnce() {
        phone.handle("1:pause");
        assertTrue(phone.isPausePressed());
        assertFalse(phone.isPausePressed());
    }

    @Test
    void stalePauseIsDropped() {
        phone.handle("1:pause");
        now += PhoneInput.PAUSE_TIMEOUT_MS + 1;
        assertFalse(phone.isPausePressed());
    }

    @Test
    void combinedInputIsActiveWhenEitherSourceIs() {
        CombinedInput both = new CombinedInput(() -> false, phone);
        assertFalse(both.isThrustPressed());
        phone.handle("1:down");
        assertTrue(both.isThrustPressed());
    }

    @Test
    void menuKeysAreForwardedToTheKeyboard() {
        java.util.List<javafx.scene.input.KeyCode> keys = new java.util.ArrayList<>();
        PhoneInput nav = new PhoneInput(() -> now, keys::add);
        nav.handle("1:key:DOWN");
        nav.handle("2:key:ENTER");
        assertEquals(java.util.List.of(javafx.scene.input.KeyCode.DOWN, javafx.scene.input.KeyCode.ENTER), keys);
    }

    @Test
    void otherKeysAreRefused() {
        java.util.List<javafx.scene.input.KeyCode> keys = new java.util.ArrayList<>();
        PhoneInput nav = new PhoneInput(() -> now, keys::add);
        nav.handle("1:key:A");
        nav.handle("2:key:NOPE");
        assertTrue(keys.isEmpty());
    }

    @Test
    void injectedKeyIsSeenAtNextPoll() {
        KeyboardInput keyboard = new KeyboardInput();
        keyboard.inject(javafx.scene.input.KeyCode.UP);
        assertFalse(keyboard.wasPressed(javafx.scene.input.KeyCode.UP));
        keyboard.poll();
        assertTrue(keyboard.wasPressed(javafx.scene.input.KeyCode.UP));
        keyboard.poll();
        assertFalse(keyboard.wasPressed(javafx.scene.input.KeyCode.UP));
    }

    @Test
    void qrCodeIsSquare() {
        boolean[][] qr = QrCode.encode("http://192.168.1.10:8080/");
        assertTrue(qr.length > 0);
        assertEquals(qr.length, qr[0].length);
    }
}
