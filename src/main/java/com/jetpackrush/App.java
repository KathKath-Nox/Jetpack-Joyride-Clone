package com.jetpackrush;

import java.io.IOException;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Point d'entrée de Jetpack Joyride : ouvre la fenêtre et lance la boucle de jeu.
 */
public class App extends Application {

    /** Largeur logique du jeu en pixels (la fenêtre peut être agrandie, le jeu est mis à l'échelle). */
    public static final int WIDTH = 1280;

    /** Hauteur logique du jeu en pixels (la fenêtre peut être agrandie, le jeu est mis à l'échelle). */
    public static final int HEIGHT = 720;

    /** Largeur minimale de la fenêtre (même ratio 16:9). */
    private static final double MIN_WIDTH = 640;

    /** Hauteur minimale de la fenêtre (même ratio 16:9). */
    private static final double MIN_HEIGHT = 360;

    /** Plafond du temps d'une frame, pour éviter les sauts de physique. */
    private static final double MAX_DT = 0.05;

    private GraphicsContext gc;
    private Game game;

    /**
     * Crée la fenêtre et le jeu, puis démarre la boucle.
     *
     * @param stage la fenêtre fournie par JavaFX
     */
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        this.gc = canvas.getGraphicsContext2D();

        // Le Group garde le jeu en 1280x720 ; le StackPane le centre dans la fenêtre
        // (bandes noires si le ratio de la fenêtre n'est pas 16:9).
        Group content = new Group(new Pane(canvas));
        StackPane root = new StackPane(content);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Mise à l'échelle proportionnelle : le plus petit des deux ratios conserve le 16:9
        content.scaleXProperty().bind(Bindings.min(
                scene.widthProperty().divide(WIDTH),
                scene.heightProperty().divide(HEIGHT)));
        content.scaleYProperty().bind(content.scaleXProperty());

        // Le jeu s'ouvre sur le MENU ; « Quitter » dans le menu ferme la fenêtre
        KeyboardInput input = new KeyboardInput(scene);
        this.game = new Game(input, Platform::exit, BestScore.persistent());
        game.getSoundSettings().persistTo(
            java.nio.file.Paths.get(System.getProperty("user.home"), ".jetpackrush", "sound.properties"));
        game.startMusic();
        // Manette téléphone : si le serveur ne démarre pas (pas de réseau...), on joue au clavier
        try {
            PhoneInput phone = new PhoneInput(input::inject);
            PhoneServer phoneServer = PhoneServer.start(phone);
            game.usePhone(phone, phoneServer::getUrl);
            phoneServer.startTunnel();
            stage.setOnHidden(e -> phoneServer.stop());
        } catch (IOException e) {
            System.err.println("Manette téléphone indisponible : " + e.getMessage());
        }
        // Alt+Tab avec une touche enfoncée : le relâchement n'arrive jamais, on relâche tout
        stage.focusedProperty().addListener((obs, was, focused) -> {
            if (!focused) {
                input.releaseAll();
            }
        });

        // F11 : bascule en plein écran
        scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
        // Échap sert déjà à la pause : il ne doit pas aussi quitter le plein écran
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");

        stage.setTitle("Jetpack Joyride");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.show();
        startLoop();
    }

    private void startLoop() {
        new AnimationTimer() {
            private long last = 0;

            @Override
            public void handle(long now) {
                if (last != 0) {
                    update(Math.min((now - last) / 1_000_000_000.0, MAX_DT));
                }
                last = now;
                render();
            }
        }.start();
    }

    private void update(double dt) {
        game.update(dt);
    }

    private void render() {
        game.render(gc);
    }

    /**
     * Lance l'application.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
    public static void main(String[] args) {
        launch(args);
    }
}