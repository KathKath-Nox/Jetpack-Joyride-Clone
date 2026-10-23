package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PlayerSkinTest {

    @Test
    void playerStartsWithTheOriginalSkin() {
        Player player = new Player(0, 0, new KeyboardInput());
        assertEquals(PlayerSprite.PATH, player.getSkin());
    }

    @Test
    void playStateAppliesTheChosenSkin() {
        Game game = new Game(new KeyboardInput(), () -> { });
        CharacterChoice choice = game.getCharacterChoice();
        choice.next();
        PlayState play = new PlayState(game, new KeyboardInput());
        play.useSkin(choice.getSheetPath());
        assertEquals("/assets/player_sprite_sheet_red.png", play.getPlayer().getSkin());
    }
}
