package com.jetpackrush;

import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import javafx.scene.input.KeyCode;

/**
 * Entrée téléphone (pattern Adapter) : implémentation de {@link InputSource} alimentée par les
 * messages que la page web du téléphone envoie à {@link PhoneServer}.
 *
 * <p>Un message a la forme {@code numéro:commande}. Les commandes sont {@code down} (doigt posé,
 * renvoyé régulièrement tant qu'il reste posé), {@code up} (doigt levé) et {@code pause}.
 * Le numéro augmente à chaque message : un message arrivé en retard est ignoré. Comme un
 * {@code up} peut se perdre, la poussée s'arrête d'elle-même si aucun {@code down} n'arrive
 * pendant {@link #HOLD_TIMEOUT_MS}.
 */
public class PhoneInput implements InputSource {

    /** Durée de validité d'un {@code down}, en millisecondes. */
    static final long HOLD_TIMEOUT_MS = 500;
    /** Durée pendant laquelle une demande de pause attend d'être lue, en millisecondes. */
    static final long PAUSE_TIMEOUT_MS = 500;

    /** Touches que le téléphone peut simuler dans les menus. */
    static final Set<KeyCode> MENU_KEYS = Set.of(
        KeyCode.UP, KeyCode.DOWN, KeyCode.LEFT, KeyCode.RIGHT, KeyCode.ENTER, KeyCode.ESCAPE);

    private final LongSupplier clock;
    private final Consumer<KeyCode> keys;
    private final AtomicLong lastDown = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong pauseAt = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong lastSeq = new AtomicLong(Long.MIN_VALUE);

    /** Crée une entrée sans navigation dans les menus. */
    public PhoneInput() {
        this(key -> { });
    }

    /**
     * @param keys reçoit les touches de menu simulées par le téléphone
     */
    public PhoneInput(Consumer<KeyCode> keys) {
        this(System::currentTimeMillis, keys);
    }

    PhoneInput(LongSupplier clock) {
        this(clock, key -> { });
    }

    PhoneInput(LongSupplier clock, Consumer<KeyCode> keys) {
        this.clock = clock;
        this.keys = keys;
    }

    /**
     * Applique un message reçu du téléphone. Appelé depuis le fil du serveur.
     *
     * @param message le message {@code numéro:commande} ; un message mal formé est ignoré
     */
    public void handle(String message) {
        int colon = message.indexOf(':');
        if (colon < 0) {
            return;
        }
        long seq;
        try {
            seq = Long.parseLong(message.substring(0, colon).trim());
        } catch (NumberFormatException e) {
            return;
        }
        String command = message.substring(colon + 1).trim();
        synchronized (this) {
            if (seq <= lastSeq.get()) {
                return;
            }
            lastSeq.set(seq);
            switch (command) {
                case "down" -> lastDown.set(clock.getAsLong());
                case "up" -> lastDown.set(Long.MIN_VALUE);
                case "pause" -> pauseAt.set(clock.getAsLong());
                default -> pressMenuKey(command);
            }
        }
    }

    private void pressMenuKey(String command) {
        if (!command.startsWith("key:")) {
            return;
        }
        try {
            KeyCode key = KeyCode.valueOf(command.substring(4));
            if (MENU_KEYS.contains(key)) {
                keys.accept(key);
            }
        } catch (IllegalArgumentException e) {
            // touche inconnue : ignorée
        }
    }

    @Override
    public boolean isThrustPressed() {
        long down = lastDown.get();
        return down != Long.MIN_VALUE && clock.getAsLong() - down < HOLD_TIMEOUT_MS;
    }

    @Override
    public boolean isPausePressed() {
        long at = pauseAt.getAndSet(Long.MIN_VALUE);
        return at != Long.MIN_VALUE && clock.getAsLong() - at < PAUSE_TIMEOUT_MS;
    }
}
