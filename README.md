<h1 align="center">🚀 Jetpack Rush</h1>

<p align="center">
  <b>An infinite 2D runner inspired by Jetpack Joyride, built with Java 17 and JavaFX.</b><br>
  Rush through the lab, dodge hazards, collect coins and beat your record!
</p>

---

## The game

You are escaping from a laboratory with a jetpack. The scenery scrolls faster and faster and hazards
keep coming: go up, go down and dodge to get as far as you can.

- Smash through the lab wall to start your escape
- Dodge lasers, zappers and missiles
- Collect coins and power-ups (shield, vehicle)
- Beat your best distance score
- Choose your character's skin before you start

## Controls

| Key | Action |
|---|---|
| `Space` | Jetpack thrust (short press: jump, held: fly). In a vehicle: jump |
| `Esc` | Pause / resume in game, quit from the menus and the game over screen |
| `↑` `↓` `←` `→` | Menu navigation |
| `Enter` or `Space` | Confirm a choice, replay |
| `F11` | Fullscreen / windowed |

### Rebindable keys

Don't like the default keys? In the main menu, choose **SETTINGS** to open the keys screen:

1. Select **Jetpack** or **Pause** with `↑` `↓`, then confirm with `Enter` or `Space`.
2. Press the new key: it is assigned immediately.
3. **Reset** restores the default keys (`Space` and `Esc`), **Back** (or `Esc`) returns to the menu.

A key already used by another action is refused ("ALREADY USED BY PAUSE"), as are the keys reserved for
menus (`Enter`, the arrows) and fullscreen (`F11`).
In the menus, `Esc` always quits or goes back.

In the code: the `KeyBindings` class manages action → key associations (without depending on JavaFX,
so it is testable), `KeyboardInput` reads it every frame and `ControlsState` is the menu screen.

### Resizable window

The window can be enlarged, shrunk or put in fullscreen. The game keeps its **16:9** ratio (1280×720):
the image is scaled proportionally, without distorting the sprites, and black bars appear on the sides
(or at the top and bottom) if the window does not have the same ratio. The menu, the game, the pause
and the game over screen adapt in the same way. Minimum size: 640×360.

### Character

In the main menu, **CUSTOMIZE** opens the character selection screen. The character is displayed
large in the center: `←` `→` change the skin (Blue, Red, Green, Pink, King), `Enter` or `Esc`
returns to the menu. The chosen skin is applied to every new game of the session.

In the code: `CharacterChoice` stores the chosen skin (without depending on JavaFX, so it is testable),
`Game` keeps it from one game to the next, `CustomizeState` is the screen and `PlayerSprite` draws the skin's sprite sheet.

## Game rules

- The score is the distance traveled (in meters); coins are shown in the HUD.
- If a hazard hits the player, they die, unless protected by a shield or a vehicle (which absorbs the hit).
- The best score is shown at the top right and recalled at the end of the game ("NEW RECORD!" if beaten).
  It is saved in `~/.jetpackrush/highscore.txt` and reloaded at each launch; deleting this file resets the record to zero.

### Hazards

| Hazard | How to avoid it |
|---|---|
| **Zapper** | An electric bar, fixed or rotating, in several sizes and orientations. Go around it. |
| **Missile** | A warning appears and follows you, then the missile rushes straight at you. Move as soon as you see it! |
| **Laser** | A warning blinks, then a beam fires for a short time. Get out of the line. |

---

## Requirements

- **Java 17** (JDK)
- **Maven 3.6+** (3.8+ recommended)
- A graphical environment (on WSL: WSLg or an X server)

JavaFX does not need to be installed: Maven downloads it automatically.

```bash
java -version    # must show 17
mvn -version
```

## Installation

```bash
git clone git@github.com:EpitechBachelorPromo2028/B-OOP-301-NCE-3-1-2dgame-1.git
cd B-OOP-301-NCE-3-1-2dgame-1
mvn clean install
```

## Running

```bash
mvn javafx:run
```

The "Jetpack Rush" window opens on the main menu.

---

## Architecture

Source code is in `src/main/java/com/jetpackrush/`, tests in `src/test/java/`, assets in `src/main/resources/assets/`.

### Class hierarchy

```
Entity
 ├── Player
 ├── Item (implements Collidable)
 │    └── PowerUp
 └── Obstacle (Laser, Zapper, Missile)
```

Main interfaces: `Collidable`, `InputSource`, `PlayerState`, `PowerUpEffect`, `ScoreListener`.
Attributes are private and exposed through accessors; overrides are marked `@Override`.

### Design patterns

| Pattern | Where | Why |
|---|---|---|
| State | `PlayerState`: `Falling`, `Flying`, `Riding`, `Dead` | Each player state handles its own physics and animation, without cascading `if`s |
| Strategy | `PowerUpEffect`: `ShieldEffect`, `VehicleEffect` | Add a power-up without modifying `Player` |
| Adapter | `InputSource` / `KeyboardInput` | Decouples the game from the keyboard (other input sources possible, tests without JavaFX) |
| Observer | `ScoreManager` / `ScoreListener` / `ScoreHud` | The HUD reacts to score changes without depending on the logic |
| Factory / Pool | `ObstacleFactory`, `ObstaclePool` | Centralized creation and reuse of obstacles |
| Game State | `MenuState`, `PlayState`, `PauseState`, `GameOverState`, `ControlsState`, `CustomizeState` | Each screen is a class, `Game` switches from one to another |

### Rendering and physics

- Game loop with variable `dt` (in seconds); movements are `speed × dt`.
- Rendering through `Canvas` / `GraphicsContext` with sprite sheets; lazy asset loading so the logic stays testable without JavaFX.
- `GameSpeed` is the single source of the scrolling speed.
- `BestScore` is injected into `Game`: in memory in the tests, saved to a file in the game.

## Project documentation

| Document | Content |
|---|---|
| [`docs/gdd.md`](docs/gdd.md) | Game Design Document: concept, rules, schedule, task distribution |
| [`docs/interface.md`](docs/interface.md) | Interface: menus, HUD, screens |
| [`docs/uml.md`](docs/uml.md) | UML diagrams (classes and states) |

The Javadoc is generated with `mvn clean javadoc:javadoc` (output in `target/site/apidocs/index.html`).

## Tests and coverage

```bash
mvn test
```

The JaCoCo report is generated in `target/site/jacoco/index.html`. The goal is 70% coverage
of the game logic; display and input classes (`App`, `KeyboardInput`, `PlayerSprite`,
`PowerUpIndicator`) are excluded from the calculation in `pom.xml`. The drawing tests use Mockito to
mock the `GraphicsContext`: no window is needed.

## Assets and licenses

All graphic and sound assets are **provided by Epitech's teaching staff** (reference repositories for
Jetpack Joyride and the "Asset Bundles" pack), and used only within the educational scope of this project.
No asset was purchased or downloaded outside of these sources.

<details>
<summary>Full asset list</summary>

| Asset | Origin | License |
|---|---|---|
| `background.png` | Provided by the teaching staff | Educational use |
| `background2.png` | Provided by the teaching staff | Educational use |
| `coin_pickup_1.wav` | Provided by the teaching staff | Educational use |
| `coins_sprite_sheet.png` | Provided by the teaching staff | Educational use |
| `dud_zapper_pop.wav` | Provided by the teaching staff | Educational use |
| `fx_explosion.png` | Provided by the teaching staff | Educational use |
| `fx_smoke.png` | Provided by the teaching staff | Educational use |
| `intro_room.png` | Provided by the teaching staff | Educational use |
| `jetpack.ogg` | Provided by the teaching staff | Educational use |
| `jetpack_font.ttf` | Provided by the teaching staff | Educational use |
| `jetpack_lp.wav` | Provided by the teaching staff | Educational use |
| `jetpack_start.wav` | Provided by the teaching staff | Educational use |
| `jetpack_stop.wav` | Provided by the teaching staff | Educational use |
| `junkyard_double.png` | Provided by the teaching staff | Educational use |
| `laserbeam.png` | Provided by the teaching staff | Educational use |
| `laseremitter.png` | Provided by the teaching staff | Educational use |
| `laserfiring.png` | Provided by the teaching staff | Educational use |
| `missile.png` | Provided by the teaching staff | Educational use |
| `missile_coming.png` | Provided by the teaching staff | Educational use |
| `missile_target.png` | Provided by the teaching staff | Educational use |
| `player_sprite_sheet.png` | Provided by the teaching staff | Educational use |
| `player_sprite_sheet_red.png`, `_green.png`, `_pink.png`, `_king.png` | Derived from `player_sprite_sheet.png` by the team | Educational use |
| `powerup_box.png` | Provided by the teaching staff | Educational use |
| `scientist_crouching.png` | Provided by the teaching staff | Educational use |
| `scientist_fainting.png` | Provided by the teaching staff | Educational use |
| `scientist_fainting_upsidedown.png` | Provided by the teaching staff | Educational use |
| `scientist_moving.png` | Provided by the teaching staff | Educational use |
| `scientist_stationary.png` | Provided by the teaching staff | Educational use |
| `shield_bubble.png` | Provided by the teaching staff | Educational use |
| `swamp_double.png` | Provided by the teaching staff | Educational use |
| `theme.ogg` | Provided by the teaching staff | Educational use |
| `title-bg.png` | Provided by the teaching staff | Educational use |
| `vehicle_ground.png` | Provided by the teaching staff | Educational use |
| `vehicle_ground2.png` | Provided by the teaching staff | Educational use |
| `vehicle_rising.png` | Provided by the teaching staff | Educational use |
| `zapper_sprite_sheet.png` | Provided by the teaching staff | Educational use |

</details>

Notes:

- `shield_bubble.png` is derived from `barry_shield.png` (character erased).
- The `player_sprite_sheet_*.png` skins are generated by script from `player_sprite_sheet.png` (outfit recolored, crown drawn for the King skin).
- `fx_explosion` and `fx_smoke` come from the "pixelartrpgvfx" effects pack.
- `jetpack_font.ttf` ("New Athletic M54") is used for the HUD.
- `coins_sprite_sheet.png` is used by the HUD for the coin icon.

The project's source code is the team's property and intended for educational use.
Game inspired by *Jetpack Joyride* (Halfbrick Studios), a school project with no commercial purpose.

## 👥 Team

| Role | Member | Scope |
|---|---|---|
| A | Sofia | Player, states, input, power-ups, jetpack particles |
| B | Sarra | Hazards: lasers, zappers, missiles, factory and pool, scientists, scenery |
| C | Kathleen | World, progression, score, screens, HUD |

<details>
<summary>Git workflow</summary>

One branch or a direct commit on `main` per feature, `git pull --rebase origin main` before
each push, pull requests with `Closes #N`. Commit messages in English, prefixed with `[+]` (addition),
`[-]` (removal), `[~]` (modification).

</details>
