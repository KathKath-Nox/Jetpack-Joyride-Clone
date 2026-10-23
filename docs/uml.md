# UML : classes et design patterns

Diagrammes de classes de Jetpack Joyride, découpés par zone pour rester lisibles. Ils reprennent l'architecture du GDD (`docs/GDD.md`) et font apparaître chaque design pattern.

| Pattern | Utilisation | Responsable | Schéma |
| --- | --- | --- | --- |
| State | États du joueur (`Flying`, `Falling`, `Riding`, `Dead`) | A | 2 |
| Strategy | Effets des power-ups (`PowerUpEffect`) | A | 2 |
| Adapter | Manette téléphone derrière `InputSource` (bonus) | A | 2 |
| Factory | Création des obstacles (`ObstacleFactory`) | B | 3 |
| Strategy | Comportement des obstacles (`ObstacleBehavior`) | B | 3 |
| Object Pool | Réutilisation des obstacles (`ObstaclePool`) | B | 3 |
| Observer | Score et pièces vers le HUD (`GameEventPublisher`, `GameEventListener`) | C | 4 |
| State | Menu, jeu, pause, fin de partie (`GameState`) | C | 4 |

## 1. Vue d'ensemble : héritage et polymorphisme

`Player`, `Obstacle` et `Item` héritent d'`Entity`. `Obstacle` et `Item` implémentent `Collidable` et réagissent chacun à leur manière à une collision avec le joueur.

```mermaid
classDiagram
    class Entity {
        <<abstract>>
        +update(double dt)
        +render(GraphicsContext gc)
        +getHitbox() Hitbox
    }
    class Collidable {
        <<interface>>
        +onCollision(Player p)
    }
    class Player
    class Obstacle {
        <<abstract>>
    }
    class Item {
        <<abstract>>
    }
    class PowerUp

    Entity <|-- Player
    Entity <|-- Obstacle
    Entity <|-- Item
    Collidable <|.. Obstacle
    Collidable <|.. Item

    Obstacle <|-- Zapper
    Obstacle <|-- Missile
    Obstacle <|-- Laser

    Item <|-- Coin
    Item <|-- PowerUp
```

## 2. Joueur (A) : State, Strategy, Adapter

- **State** : le comportement du joueur change selon son état courant.
- **Strategy** : un power-up délègue son effet à un `PowerUpEffect`.
- **Adapter** : `Player` lit l'entrée via `InputSource`, jamais le clavier directement. Le clavier est la première implémentation, la manette téléphone (bonus) est la deuxième.

```mermaid
classDiagram
    class Player
    class PlayerState {
        <<interface>>
        +update(Player p, double dt)
    }
    class Item {
        <<abstract>>
    }
    class PowerUp
    class PowerUpEffect {
        <<interface>>
        +apply(Player p)
        +expire(Player p)
    }
    class InputSource {
        <<interface>>
        +isThrustPressed() boolean
    }

    Player --> PlayerState : état courant
    PlayerState <|.. Flying
    PlayerState <|.. Falling
    PlayerState <|.. Riding
    PlayerState <|.. Dead

    Item <|-- PowerUp
    PowerUp --> PowerUpEffect
    PowerUpEffect <|.. ShieldEffect
    PowerUpEffect <|.. VehicleEffect

    Player ..> InputSource : lit
    InputSource <|.. KeyboardInput
    InputSource <|.. PhoneInput

    note for PlayerState "Pattern State"
    note for PowerUpEffect "Pattern Strategy"
    note for InputSource "Pattern Adapter (PhoneInput : bonus)"
```

## 3. Dangers (B) : Strategy, Factory, Object Pool

- **Strategy** : chaque obstacle délègue son déplacement à une `ObstacleBehavior`.
- **Factory** : `ObstacleFactory.create(type, x, y)` est le seul endroit où l'on construit un obstacle.
- **Object Pool** : `ObstaclePool` réutilise les obstacles sortis de l'écran au lieu d'en recréer.

```mermaid
classDiagram
    class Obstacle {
        <<abstract>>
    }
    class ObstacleBehavior {
        <<interface>>
        +move(Obstacle o, double dt)
    }
    class ObstacleFactory {
        +create(type, x, y) Obstacle
    }
    class ObstaclePool
    class LevelGenerator
    class GameSpeed {
        +getSpeed() double
    }

    Obstacle <|-- Zapper
    Obstacle <|-- Missile
    Obstacle <|-- Laser

    Obstacle --> ObstacleBehavior
    ObstacleBehavior <|.. StaticBehavior
    ObstacleBehavior <|.. TrackingBehavior
    ObstacleBehavior <|.. TimedBehavior

    ObstacleFactory ..> Obstacle : crée
    ObstaclePool --> ObstacleFactory
    LevelGenerator --> ObstaclePool
    Obstacle ..> GameSpeed : lit

    note for ObstacleBehavior "Pattern Strategy"
    note for ObstacleFactory "Pattern Factory"
    note for ObstaclePool "Pattern Object Pool"
```

## 4. Monde (C) : Observer, State, progression

- **Observer** : le joueur et les pièces publient des événements (pièce ramassée, mort), que le score et le HUD écoutent sans que le joueur les connaisse.
- **State** : `Game` délègue à l'état courant (menu, jeu, pause, fin de partie).
- **Vitesse du décor** : une seule source, `GameSpeed`, écrite par `DifficultyManager` et lue par tout le monde.

```mermaid
classDiagram
    class Player
    class Coin
    class GameEventPublisher {
        +subscribe(GameEventListener l)
        +publish(GameEvent e)
    }
    class GameEventListener {
        <<interface>>
        +onEvent(GameEvent e)
    }
    class ScoreManager
    class HUD
    class Game
    class GameState {
        <<interface>>
    }
    class DifficultyManager
    class GameSpeed {
        +getSpeed() double
    }
    class LevelGenerator

    Player ..> GameEventPublisher : publie
    Coin ..> GameEventPublisher : publie
    GameEventPublisher o-- GameEventListener
    GameEventListener <|.. ScoreManager
    GameEventListener <|.. HUD

    Game --> GameState
    GameState <|.. MenuState
    GameState <|.. PlayState
    GameState <|.. PauseState
    GameState <|.. GameOverState

    DifficultyManager --> GameSpeed : écrit
    LevelGenerator ..> GameSpeed : lit

    note for GameEventPublisher "Pattern Observer"
    note for GameState "Pattern State"
```
