package com.jetpackrush;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CharacterChoiceTest {

    @Test
    void startsOnTheOriginalSkin() {
        CharacterChoice choice = new CharacterChoice();
        assertEquals("BLEU", choice.getName());
        assertEquals("/assets/player_sprite_sheet.png", choice.getSheetPath());
    }

    @Test
    void nextMovesToTheFollowingSkin() {
        CharacterChoice choice = new CharacterChoice();
        choice.next();
        assertEquals("ROUGE", choice.getName());
    }

    @Test
    void nextWrapsAroundAfterTheLastSkin() {
        CharacterChoice choice = new CharacterChoice();
        for (int i = 0; i < choice.count(); i++) {
            choice.next();
        }
        assertEquals("BLEU", choice.getName());
    }

    @Test
    void previousWrapsAroundBeforeTheFirstSkin() {
        CharacterChoice choice = new CharacterChoice();
        choice.previous();
        assertEquals("ROI", choice.getName());
    }

    @Test
    void everySkinHasAnExistingSheet() {
        CharacterChoice choice = new CharacterChoice();
        for (int i = 0; i < choice.count(); i++) {
            assertNotNull(getClass().getResource(choice.getSheetPath()), choice.getSheetPath());
            choice.next();
        }
    }
}
