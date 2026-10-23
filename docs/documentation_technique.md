# Documentation technique : Jetpack Rush

Ce document décrit le fonctionnement interne du jeu : architecture, classes, choix de conception, tests et façon d'étendre le code. Le README couvre l'installation et l'utilisation ; la Javadoc (`mvn clean javadoc:javadoc`) détaille chaque méthode.

## 1. Vue d'ensemble

| Élément | Choix |
| --- | --- |
| Langage | Java 17 |
| Interface graphique | JavaFX 17 (`Canvas` et `GraphicsContext`) |
| Build | Maven (`javafx-maven-plugin`, `jacoco-maven-plugin`) |
| Tests | JUnit 5, Mockito pour simuler le `GraphicsContext` |
| Couverture | JaCoCo 0.8.12 |
| Paquet | `com.jetpackrush` (code dans `src/main/java`, tests dans `src/test/java`, assets dans `src/main/resources/assets`) |
| Fenêtre | 1280 × 720 (16:9), redimensionnable, minimum 640 × 360, plein écran avec `F11` |

Le jeu est un runner infini : le décor défile vers la gauche de plus en plus vite, le joueur contrôle seulement la hauteur de son personnage avec le jetpack, et le score est la distance parcourue.

Principe directeur : **la logique est séparée de l'affichage**. Toute la physique, les états, les collisions, le score et les menus sont des classes testables sans fenêtre. Les images et les polices sont chargées au premier `render`, jamais dans un constructeur, car JavaFX ne peut pas créer d'`Image` sans fenêtre ouverte.

## 2. Boucle de jeu

`App` crée la fenêtre et lance une boucle qui, à chaque image :

1. calcule `dt`, le temps écoulé depuis l'image précédente, en secondes (environ 0,0167 s à 60 images par seconde) ;
2. appelle `game.update(dt)`, qui met à jour l'écran courant ;
3. appelle `game.render(gc)`, qui le dessine.

Toutes les vitesses sont exprimées par seconde et multipliées par `dt` (`position += vitesse × dt`). Le jeu va ainsi à la même vitesse quelle que soit la fréquence d'affichage de la machine.

`GameSpeed` est l'unique source de la vitesse de défilement : décor, obstacles, pièces et power-ups la lisent tous, ce qui garantit qu'ils avancent ensemble.

## 3. Écrans du jeu (pattern Game State)

Chaque écran est une classe qui implémente `GameState` (`update(double dt)` et `render(GraphicsContext gc)`). `Game` garde l'écran courant et fournit les méthodes de transition.

| Écran | Rôle | Transitions |
| --- | --- | --- |
| `MenuState` | Menu principal : JOUER, QUITTER, RÉGLAGES, CUSTOMISER | `startNewGame()`, `quit()`, `showControls()`, `showCustomize()` |
| `PlayState` | La partie : joueur, obstacles, pièces, power-ups, décor, HUD | `pause(play)`, `gameOver(play)` |
| `PauseState` | Pause par-dessus la partie en cours | `resume(play)` et les autres options du menu de pause |
| `GameOverState` | Fin de partie : distance, meilleur score, « NOUVEAU RECORD ! » | `startNewGame()` (rejouer), `quit()` |
| `ControlsState` | Écran des touches (bouton RÉGLAGES) | `backToMenu()` |
| `CustomizeState` | Choix du skin du personnage | `backToMenu()` |

```
            ┌──────────── backToMenu ────────────┐
            ▼                                     │
  ┌──────────────┐ showControls  ┌───────────────┐│
  │  MenuState   │──────────────►│ ControlsState ├┘
  │              │ showCustomize ┌───────────────┐
  │              │──────────────►│CustomizeState ├─► backToMenu
  └──────┬───────┘               └───────────────┘
         │ startNewGame
         ▼
  ┌──────────────┐   pause    ┌────────────┐
  │  PlayState   │◄──────────►│ PauseState │
  └──────┬───────┘   resume   └────────────┘
         │ gameOver
         ▼
  ┌──────────────┐ startNewGame (rejouer)
  │GameOverState │
  └──────────────┘
```

`Game` détient aussi ce qui doit survivre d'une partie à l'autre : le meilleur score (`BestScore`) et le skin choisi (`CharacterChoice`). Une nouvelle `PlayState` est créée à chaque partie, ce qui remet tout le reste à zéro.

## 4. Hiérarchie des classes

```
Entity (abstraite)
 ├── Player
 ├── Item (abstraite, implements Collidable)
 │    └── PowerUp
 └── Obstacle (implements Collidable)
      ├── Laser
      ├── Zapper
      └── Missile
```

| Interface | Rôle | Implémentations |
| --- | --- | --- |
| `Collidable` | Réagir quand le joueur touche l'objet (`onCollision(Player)`) | `Item`, `Obstacle` |
| `InputSource` | Savoir si la poussée est demandée (`isThrustPressed()`) | `KeyboardInput` |
| `PlayerState` | Comportement du joueur selon son état (`update(Player, dt)`) | `Falling`, `Flying`, `Riding`, `Dead` |
| `PowerUpEffect` | Effet d'un power-up (`apply(Player)`, `expire(Player)`) | `ShieldEffect`, `VehicleEffect` |
| `ScoreListener` | Être prévenu des changements de score | `ScoreHud` |
| `GameState` | Un écran du jeu | les 6 écrans |

Les attributs sont privés et exposés par des accesseurs. Les méthodes redéfinies sont marquées `@Override`.

## 5. Le joueur

### 5.1 Physique du jetpack

`Player` a une vitesse verticale `vy` (positive vers le bas, comme l'axe y de l'écran). À chaque image :

1. l'accélération vaut la gravité, moins la poussée si le jetpack est actif ;
2. `vy` est plafonnée à `MAX_SPEED` dans les deux sens ;
3. la position change de `vy × dt` ;
4. le sol et le plafond bloquent le joueur et remettent `vy` à 0, sans le tuer.

| Constante | Valeur | Rôle |
| --- | --- | --- |
| `GRAVITY` | 1400 px/s² | Tire le joueur vers le bas |
| `THRUST` | 2800 px/s² | Poussée du jetpack (plus forte que la gravité, donc le joueur monte) |
| `MAX_SPEED` | 500 px/s | Vitesse verticale maximale |
| `JUMP_SPEED` | 850 px/s | Vitesse du saut en véhicule (environ 258 px de hauteur) |
| `GROUND_HEIGHT` | 80 px | Hauteur du sol depuis le bas de l'écran |

L'inertie vient de `vy` : quand on relâche la poussée, le joueur continue de monter un instant avant que la gravité ne l'emporte. Le jetpack et le véhicule partagent la même méthode interne de déplacement (`step`), avec leurs propres accélérations.

### 5.2 États du joueur (pattern State)

`Player` délègue son comportement à son état courant (`state.update(this, dt)`) et ne contient aucun `if` sur l'état.

| État | Comportement | Sortie |
| --- | --- | --- |
| `Falling` | Gravité seule | Poussée : `Flying` |
| `Flying` | Gravité et poussée, flammes et douilles émises | Relâchement : `Falling` |
| `Riding` | Véhicule pendant 10 s (`DURATION`). Appui court (moins de 0,2 s, `HOLD_DELAY`) : saut. Appui maintenu : jetpack du véhicule | Fin du temps ou choc : `Falling` |
| `Dead` | Ignore l'entrée, retombe, reste mort | Aucune (état final) |

Ajouter un état revient à écrire une nouvelle classe, sans modifier les autres.

### 5.3 Chocs : `hit()` et `die()`

Les obstacles appellent toujours `player.hit()`, jamais `die()` directement. `hit()` applique l'ordre de protection :

1. en véhicule : le véhicule est détruit (explosion, débris, fumée), le joueur survit et garde son bouclier ;
2. sinon, avec un bouclier : le bouclier est consommé, le joueur survit ;
3. sinon : `die()`.

`die()` ignore un second appel quand le joueur est déjà mort, pour ne pas relancer les effets.

### 5.4 Hitbox et sprite

La hitbox du joueur fait **40 × 60 px**, le sprite est dessiné en **90 × 90 px** autour d'elle. Les collisions ne dépendent donc pas de la taille de l'image. `Hitbox.intersects` considère que deux boîtes qui se touchent seulement par un bord ne se croisent pas.

`PlayerSprite` lit une feuille de 4 colonnes × 6 lignes (environ 134 px par case). La ligne dépend de l'état (vol pour `Flying`, électrocuté pour `Dead`, chute sinon) et la colonne avance 12 fois par seconde. Pendant le véhicule, c'est l'image du véhicule qui est dessinée, avec le pilote dans la cabine.

### 5.5 Entrées (pattern Adapter)

Le joueur ne lit jamais le clavier : il interroge une `InputSource`. `KeyboardInput` est l'adaptateur clavier. Une autre source (manette, téléphone, IA de test) pourrait être branchée sans toucher à `Player`, et les tests utilisent des entrées simulées.

Les touches sont configurables. `KeyBindings` associe chaque action (`Jetpack`, `Pause`) à une touche, refuse une touche déjà prise ou réservée (`Entrée`, flèches, `F11`) et peut tout réinitialiser (`Espace` et `Échap`). Elle ne dépend pas de JavaFX pour sa logique, donc elle est testée directement. `KeyboardInput` la consulte à chaque image, et `ControlsState` est l'écran qui la modifie.

## 6. Power-ups (pattern Strategy)

`PowerUp` est un `Item` qui ne connaît pas son effet : il le délègue à un `PowerUpEffect`.

| Effet | `apply` | `expire` |
| --- | --- | --- |
| `ShieldEffect` | Donne un bouclier (bulle `shield_bubble.png` autour du joueur) | Retire le bouclier |
| `VehicleEffect` | Passe le joueur en `Riding` (sauf s'il est mort) | Ramène en `Falling` |

Le bouclier et le véhicule sont donc deux `PowerUp` construits avec deux stratégies différentes. Ajouter un power-up ne demande qu'une nouvelle classe d'effet. Les boîtes apparaissent pendant la partie, défilent avec le décor, et l'effet est tiré au hasard. Un `PowerUp` ne peut être ramassé qu'une fois.

`PowerUpIndicator` affiche en jeu le temps restant du véhicule (format `07.3S`) et le bouclier actif, avec la police `jetpack_font.ttf`.

## 7. Dangers, pièces et monde

| Classe | Rôle |
| --- | --- |
| `Obstacle` | Base des dangers : défilement, collision avec la hitbox du joueur, `onCollision` qui appelle `player.hit()` |
| `Zapper` | Barre électrique fixe ou en rotation, plusieurs tailles et orientations |
| `Missile` | Alerte qui suit le joueur, puis tir en ligne droite |
| `Laser` | Avertissement clignotant, puis faisceau pendant un court instant |
| `ObstacleFactory` | Crée les obstacles (pattern Factory). Reçoit la vitesse et l'accès à la hitbox du joueur (pour viser) |
| `ObstaclePool` | Réutilise les obstacles sortis de l'écran au lieu d'en créer de nouveaux (pattern Object Pool) |
| `LevelGenerator`, `Chunk` | Génération du niveau par tronçons : un `Chunk` a un nom, une largeur et une liste de rectangles qui le composent |
| `DifficultyManager` | Fait évoluer la difficulté au fil de la partie |
| `Coin` | Pièce à ramasser, comptée dans le score |
| `Scientist` | Scientifiques du décor, qui réagissent au passage du joueur |
| `Decor`, `IntroWall` | Fond qui défile et mur du labo que le joueur traverse au départ (`player.enter()` déclenche l'explosion d'entrée) |
| `Sprites`, `AssetImages`, `Assets` | Chargement et cache des images, ouverture des ressources et de la police |

## 8. Score (pattern Observer)

`ScoreManager` calcule la distance et compte les pièces. Il prévient ses `ScoreListener` à chaque changement, sans savoir qui les affiche. `ScoreHud` est l'un de ces observateurs : il dessine la distance, les pièces et, en haut à droite, le meilleur score (« MEILLEUR : n M »). On pourrait ajouter un autre observateur (sons, succès) sans modifier `ScoreManager`.

### Meilleur score sauvegardé

`BestScore` garde la meilleure distance et indique si la dernière partie est un nouveau record. Il a deux modes :

- `new BestScore()` : en mémoire seulement, utilisé par les tests ;
- `BestScore.persistent()` : lit et écrit `~/.jetpackrush/highscore.txt` (un seul entier). Un fichier absent, invalide ou négatif donne 0, et une erreur d'écriture est ignorée pour ne jamais faire planter le jeu.

`Game` reçoit son `BestScore` par son constructeur (injection de dépendance) : seul `App` branche la version persistante, donc les tests du jeu n'écrivent jamais sur le disque. Le dossier `.jetpackrush` est caché (son nom commence par un point) et propre à chaque machine.

## 9. Personnalisation du personnage

`CharacterChoice` contient la liste des skins (nom et feuille de sprites) et le skin courant (`next`, `previous`, `getName`, `getSheetPath`). `Game` le conserve d'une partie à l'autre, `startNewGame` l'applique au joueur via `PlayState.useSkin`, et `Player` transmet le chemin de la feuille à `PlayerSprite`, qui ne la recharge que si le skin change.

| Skin | Feuille | Fabrication |
| --- | --- | --- |
| BLEU | `player_sprite_sheet.png` | Original fourni par la pédagogie |
| ROUGE, VERT, ROSE | `player_sprite_sheet_red.png`, `_green.png`, `_pink.png` | Recoloration de la tenue (pixels de teinte bleue uniquement) |
| ROI | `player_sprite_sheet_king.png` | Tenue violette et couronne dessinée sur la tête à chaque image |

Les feuilles dérivées ont été générées par un script Python (Pillow) à partir de la feuille d'origine. Elles gardent la même grille, donc toute l'animation.

`CustomizeState` affiche le personnage en grand au centre (pose de chute, agrandi 3 fois, sans lissage pour garder les pixels nets), deux flèches et le nom du skin. Les flèches changent de skin, `Entrée` ou `Échap` revient au menu.

## 10. Effets visuels

| Classe | Rôle |
| --- | --- |
| `SpriteEffect` | Joue une fois une animation de 6 images (64 × 64), puis disparaît : explosion (`fx_explosion.png`) et fumée (`fx_smoke.png`) |
| `Particle` | Un débris ou un rond : position, vitesse, gravité, rotation, durée de vie, transparence qui s'estompe. La taille est bornée à 0 |
| `ParticleSystem` | Gère les effets et les particules : `explosion`, `smoke`, `cloud`, `burst` (débris) et `jet` (flammes et douilles du jetpack) |

| Moment | Effet |
| --- | --- |
| Entrée dans le labo (`enter()`) | Explosion, débris et nuage |
| Vol (`Flying`) | Toutes les 0,02 s : deux traînées jaunes et, une fois sur trois, une douille orange |
| Véhicule détruit | Explosion, débris, nuage, fumée pendant 1,2 s |
| Mort | Nuage et fumée pendant 2 s |

Tout est piloté depuis `Player`, qui possède son `ParticleSystem`.

## 11. Récapitulatif des design patterns

| Pattern | Où | Pourquoi |
| --- | --- | --- |
| State | `PlayerState` : `Falling`, `Flying`, `Riding`, `Dead` | Le comportement change avec l'état, sans `if` en cascade, et un nouvel état ne touche pas les autres |
| Strategy | `PowerUpEffect` : `ShieldEffect`, `VehicleEffect` | Un même `PowerUp`, des effets interchangeables |
| Adapter | `InputSource` / `KeyboardInput` | Le jeu ne dépend pas du clavier ; entrées simulées dans les tests |
| Observer | `ScoreManager` / `ScoreListener` / `ScoreHud` | Le HUD réagit au score sans couplage avec la logique |
| Factory | `ObstacleFactory` | Création des obstacles centralisée |
| Object Pool | `ObstaclePool` | Réutilisation des obstacles, moins d'allocations pendant la partie |
| Game State | `GameState` et les 6 écrans | Un écran = une classe, `Game` bascule de l'un à l'autre |
| Injection de dépendance | `BestScore` passé à `Game` | Le jeu est testable sans écrire sur le disque |

Les diagrammes UML (classes et états) sont dans `docs/uml.md`.

## 12. Tests et couverture

```bash
mvn test
```

320 tests JUnit 5, 0 échec. Les tests simulent le temps (appels successifs à `update(dt)`) et les entrées, sans ouvrir de fenêtre. Les tests de dessin utilisent Mockito pour simuler le `GraphicsContext` ; ceux qui ont besoin de la boîte à outils JavaFX sont ignorés (« skipped ») quand elle n'est pas disponible.

Exemples de ce qui est vérifié :

| Domaine | Tests |
| --- | --- |
| Physique | `PlayerTest` : chute, montée, inertie, plafond, sol |
| États | `PlayerStateTest`, `RidingTest`, `RidingHoldTest` : transitions, saut court, vol maintenu |
| Power-ups | `ShieldTest`, `VehicleTest`, `PowerUpTest` : protection, ordre véhicule puis bouclier, ramassage unique |
| Effets | `ParticleTest`, `CrashEffectTest`, `IntroEffectTest` |
| Score | `ScoreManagerTest`, `BestScoreTest`, `BestScoreFileTest` (dossier temporaire `@TempDir`), `GameBestScoreTest` |
| Écrans | `MenuStateTest`, `PauseStateTest`, `GameOverStateTest`, `ControlsStateTest` |
| Touches | `KeyBindingsTest`, `KeyboardInputBindingsTest`, `KeyboardInputTest` |
| Personnage | `CharacterChoiceTest`, `PlayerSkinTest` |
| Dangers et monde | `LaserTest`, `ZapperTest`, `MissileTest`, `ObstacleFactoryTest`, `ObstaclePoolTest`, `LevelGeneratorTest`, `ChunkTest`, `DifficultyManagerTest`, `ScientistTest`, `DecorTest`, `IntroWallTest`, `CoinTest`, et leurs tests de rendu |

### Couverture

Le sujet demande une couverture de tests sans fixer de seuil. L'équipe s'est fixé dans son GDD **70 % de couverture de lignes sur la logique du jeu**, l'affichage JavaFX étant exclu car il ne se teste pas sans fenêtre. Les classes exclues dans le `pom.xml` sont `App`, `KeyboardInput`, `PlayerSprite`, `PowerUpIndicator` et `ScoreHud`.

Résultat au 15 octobre 2026 : **70,5 %** pour l'équipe. La logique du joueur (physique, états, effets de power-ups, hitbox) est couverte à 100 %. Le rapport est dans `target/site/jacoco/index.html`, et le pourcentage par classe s'obtient avec :

```bash
awk -F, 'NR>1 {t=$8+$9; if (t>0) printf "%-22s %3d%%\n", $3, 100*$9/t}' target/site/jacoco/jacoco.csv | sort
```

## 13. Assets

Tous les assets sont dans `src/main/resources/assets/` et chargés par chemin de classpath (`/assets/...`). Ils sont fournis par la pédagogie (dépôts de référence de Jetpack Joyride et pack « Asset Bundles »). Les fichiers dérivés par l'équipe sont `shield_bubble.png` (extrait de `barry_shield.png`) et les feuilles de skins `player_sprite_sheet_*.png`. La liste complète avec leur origine est dans le README.

## 14. Étendre le jeu

| Ajouter | Étapes |
| --- | --- |
| Un power-up | Écrire une classe qui implémente `PowerUpEffect` (`apply`, `expire`), puis l'ajouter au tirage des boîtes |
| Un état du joueur | Écrire une classe qui implémente `PlayerState` et la déclencher depuis l'état ou l'effet concerné |
| Un skin | Placer une feuille 4 × 6 au même format dans `assets/`, puis ajouter son nom et son chemin dans `CharacterChoice` |
| Un écran | Écrire une classe qui implémente `GameState` et une méthode de transition dans `Game` |
| Une source d'entrée | Écrire une classe qui implémente `InputSource` et la passer à `PlayState` |
| Un danger | Hériter d'`Obstacle`, le créer dans `ObstacleFactory`, et appeler `player.hit()` dans `onCollision` |

## 15. Commandes utiles

| Commande | Effet |
| --- | --- |
| `mvn javafx:run` | Compile et lance le jeu |
| `mvn test` | Lance les tests et génère le rapport JaCoCo |
| `mvn clean test` | Recompile tout avant les tests |
| `mvn clean javadoc:javadoc` | Génère la Javadoc dans `target/site/apidocs/` (0 avertissement) |