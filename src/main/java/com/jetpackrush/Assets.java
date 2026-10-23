package com.jetpackrush;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import javafx.scene.text.Font;

/**
 * Accès aux fichiers de {@code src/main/resources/assets} (images, police, sons), partagé par les écrans.
 */
final class Assets {

    private Assets() {
    }

    /**
     * Ouvre un asset. Chargé depuis un flux et pas une URL : avec une URL, JavaFX ne trouve
     * pas la police si le chemin du projet contient un espace (« 2D Game »).
     *
     * @param name nom du fichier, par exemple {@code title-bg.png}
     * @return le flux du fichier, à fermer par l'appelant
     */
    static InputStream open(String name) {
        InputStream in = Assets.class.getResourceAsStream("/assets/" + name);
        return Objects.requireNonNull(in, "Asset introuvable : /assets/" + name);
    }

    /**
     * Charge la police du jeu.
     *
     * @param size taille en points
     * @return la police à cette taille
     */
    static Font font(double size) {
        try (InputStream in = open("jetpack_font.ttf")) {
            return Font.loadFont(in, size);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
