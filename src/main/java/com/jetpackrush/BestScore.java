package com.jetpackrush;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Meilleure distance du joueur. Elle est gardée par {@link Game}, qui dure plus longtemps qu'une
 * partie, et mise à jour à chaque fin de partie. Avec un fichier, le record est rechargé au
 * lancement et réécrit à chaque nouveau record.
 */
public class BestScore {

    private final Path file;
    private int best;
    private boolean newRecord;

    /**
     * Crée un meilleur score gardé en mémoire uniquement (rien n'est lu ni écrit sur le disque).
     */
    public BestScore() {
        this.file = null;
    }

    /**
     * Crée un meilleur score sauvegardé dans un fichier. Un fichier absent ou illisible donne 0.
     *
     * @param file fichier texte contenant la meilleure distance
     */
    public BestScore(Path file) {
        this.file = file;
        this.best = load();
    }

    /**
     * Crée un meilleur score sauvegardé dans {@code .jetpackrush/highscore.txt}, dans le dossier
     * personnel de l'utilisateur.
     *
     * @return le meilleur score persistant
     */
    public static BestScore persistent() {
        return new BestScore(Paths.get(System.getProperty("user.home"), ".jetpackrush", "highscore.txt"));
    }

    /**
     * Donne la meilleure distance des parties terminées.
     *
     * @return la meilleure distance, en mètres (0 si aucune partie n'est terminée)
     */
    public int get() {
        return best;
    }

    /**
     * Indique si la dernière distance enregistrée a battu le record.
     *
     * @return {@code true} si la dernière partie terminée est un nouveau record
     */
    public boolean isNewRecord() {
        return newRecord;
    }

    /**
     * Enregistre la distance d'une partie terminée et garde la meilleure. Un nouveau record est
     * écrit dans le fichier ; une erreur d'écriture est ignorée pour ne pas interrompre le jeu.
     *
     * @param distance distance de la partie, en mètres
     */
    public void submit(int distance) {
        newRecord = distance > best;
        if (newRecord) {
            best = distance;
            save();
        }
    }

    private int load() {
        try {
            String text = Files.readString(file, StandardCharsets.UTF_8).trim();
            return Math.max(0, Integer.parseInt(text));
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
    }

    private void save() {
        if (file == null) {
            return;
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(file, Integer.toString(best), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return;
        }
    }
}
