package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.scene.image.Image;

/** Cache d'images : chaque sprite est chargé une seule fois depuis le dossier des assets. */
public final class Sprites {
    private static final String FOLDER = "/assets/";
    private static final Map<String, Optional<Image>> CACHE = new HashMap<>();

    private Sprites() {
    }

    /**
     * Renvoie une image du cache, en la chargeant au premier appel.
     *
     * @param name nom du fichier dans le dossier des assets
     * @return l'image, ou null si elle est absente ou illisible
     */
    public static Image get(String name) {
        return CACHE.computeIfAbsent(name, Sprites::read).orElse(null);
    }

    private static Optional<Image> read(String name) {
        try (InputStream in = Sprites.class.getResourceAsStream(FOLDER + name)) {
            if (in == null) {
                return Optional.empty();
            }
            Image image = new Image(in);
            return image.isError() ? Optional.empty() : Optional.of(image);
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
