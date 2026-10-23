package com.jetpackrush;

import java.util.function.Supplier;
import javafx.scene.canvas.GraphicsContext;

/**
 * Garde l'écran courant ({@link GameState}) et gère le passage d'un écran à l'autre.
 */
public class Game {

    private final KeyboardInput input;
    private final Runnable onQuit;
    private final BestScore bestScore;
    private final CharacterChoice character = new CharacterChoice();
    private final SoundSettings soundSettings = new SoundSettings();
    private final SoundEffects sounds = new SoundEffects(new MixingSoundPlayer(), soundSettings);
    private InputSource phone;
    private Supplier<String> phoneUrl = () -> null;
    private GameState state;
    private boolean rush;

    /**
     * Crée le jeu, qui démarre sur le menu principal.
     *
     * @param input  le clavier
     * @param onQuit action lancée quand le joueur choisit « Quitter » dans le menu
     */
    public Game(KeyboardInput input, Runnable onQuit) {
        this(input, onQuit, new BestScore());
    }

    /**
     * Crée le jeu avec un meilleur score fourni, par exemple sauvegardé dans un fichier.
     *
     * @param input     le clavier
     * @param onQuit    action lancée quand le joueur choisit « Quitter » dans le menu
     * @param bestScore le meilleur score à utiliser
     */
    public Game(KeyboardInput input, Runnable onQuit, BestScore bestScore) {
        this.input = input;
        this.bestScore = bestScore;
        this.onQuit = onQuit;
        // MENU : premier écran affiché au lancement, avant d'entrer dans le jeu
        this.state = new MenuState(this, input);
    }

    /** Lance la musique du jeu, en boucle (appelé au démarrage de l'application). */
    public void startMusic() {
        sounds.startMusic();
    }

    /**
     * Branche le téléphone comme seconde manette.
     *
     * @param phone l'entrée alimentée par le téléphone
     * @param url   donne l'adresse à ouvrir sur le téléphone, affichée en QR code dans le menu ;
     *              elle peut changer (réseau local, puis tunnel dès qu'il est prêt)
     */
    public void usePhone(InputSource phone, Supplier<String> url) {
        this.phone = phone;
        this.phoneUrl = url;
    }

    /**
     * @return l'adresse de la manette téléphone, ou {@code null} si elle n'est pas disponible
     */
    String getPhoneUrl() {
        return phoneUrl.get();
    }

    /**
     * Lit le clavier puis met à jour l'écran courant.
     *
     * @param dt temps écoulé depuis la frame précédente, en secondes
     */
    public void update(double dt) {
        input.poll();
        state.update(dt);
    }

    /**
     * Dessine l'écran courant.
     *
     * @param gc le contexte graphique du canvas
     */
    public void render(GraphicsContext gc) {
        state.render(gc);
    }

    /**
     * Lance une nouvelle partie (appelé par le menu, bouton « Jouer »,
     * ou par la fin de partie, bouton « Rejouer »).
     */
    public void startNewGame() {
        input.lockThrust();
        PlayState play = new PlayState(this, phone == null ? input : new CombinedInput(input, phone), rush);
        play.showBest(bestScore.get());
        play.useSkin(character.getSheetPath());
        play.addEventListener(sounds);
        state = play;
    }

    /**
     * Lance une partie en mode Rush : accélérée, avec tous les obstacles et les décors qui changent
     * vite (appelé par le menu, bouton « Rush »). « Rejouer » relance aussi une partie Rush, jusqu'au
     * retour au menu.
     */
    public void startRushGame() {
        rush = true;
        startNewGame();
    }

    /**
     * Met la partie en pause (appelé par la partie, touche Échap).
     *
     * @param play la partie à figer, reprise telle quelle par {@link #resume}
     */
    public void pause(PlayState play) {
        sounds.silenceJetpack();
        state = new PauseState(this, input, play);
    }

    /**
     * Reprend une partie mise en pause, là où elle s'était arrêtée
     * (appelé par la pause, à la fin du compte à rebours).
     *
     * @param play la partie figée par {@link #pause}
     */
    public void resume(PlayState play) {
        play.resumeSounds();
        state = play;
    }

    /**
     * Affiche l'écran de fin (appelé par la partie, une fois le joueur mort).
     *
     * @param play la partie terminée, affichée figée en fond
     */
    public void gameOver(PlayState play) {
        sounds.silenceJetpack();
        bestScore.submit(play.getScore().getDistance());
        state = new GameOverState(this, input, play);
    }

    /**
     * Abandonne la partie et revient au menu principal
     * (appelé par la pause ou la fin de partie, bouton « Quitter »).
     */
    public void backToMenu() {
        sounds.silenceJetpack();
        rush = false;
        state = new MenuState(this, input);
    }

    /** Ouvre l'écran « Réglages » pour changer les commandes (appelé par le menu). */
    public void showControls() {
        state = new ControlsState(this, input);
    }

    /** Ouvre l'écran « Son » : volume de la musique, musique et effets (appelé par les réglages). */
    public void showSound() {
        state = new SoundState(this, input, soundSettings);
    }

    /**
     * @return les réglages du son, à brancher sur un fichier avec {@link SoundSettings#persistTo}
     */
    public SoundSettings getSoundSettings() {
        return soundSettings;
    }

    /**
     * Ouvre l'écran de choix du personnage.
     */
    public void showCustomize() {
        state = new CustomizeState(this, input);
    }

    /** Ferme le jeu (appelé par le menu, bouton « Quitter »). */
    public void quit() {
        onQuit.run();
    }

    CharacterChoice getCharacterChoice() {
        return character;
    }

    BestScore getBestScore() {
        return bestScore;
    }

    GameState getState() {
        return state;
    }
}