package com.jetpackrush;

/**
 * Skin choisi pour le personnage. Il est gardé par {@link Game}, donc il reste le même d'une
 * partie à l'autre pendant la session.
 */
public class CharacterChoice {

    private static final String[] NAMES = {"BLEU", "ROUGE", "VERT", "ROSE", "ROI"};
    private static final String[] SHEETS = {
        "/assets/player_sprite_sheet.png",
        "/assets/player_sprite_sheet_red.png",
        "/assets/player_sprite_sheet_green.png",
        "/assets/player_sprite_sheet_pink.png",
        "/assets/player_sprite_sheet_king.png"
    };

    private int index;

    /**
     * Passe au skin suivant, en revenant au premier après le dernier.
     */
    public void next() {
        index = (index + 1) % NAMES.length;
    }

    /**
     * Passe au skin précédent, en revenant au dernier avant le premier.
     */
    public void previous() {
        index = (index + NAMES.length - 1) % NAMES.length;
    }

    /**
     * Donne le nom du skin courant, tel qu'affiché dans le menu.
     *
     * @return le nom de la couleur, en majuscules
     */
    public String getName() {
        return NAMES[index];
    }

    /**
     * Donne le chemin de la feuille de sprites du skin courant.
     *
     * @return le chemin de la ressource
     */
    public String getSheetPath() {
        return SHEETS[index];
    }

    /**
     * Donne le nombre de skins disponibles.
     *
     * @return le nombre de skins
     */
    public int count() {
        return NAMES.length;
    }
}
