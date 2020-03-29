package com.github.quillraven.quillysadventure.trigger

import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.configuration.Character

@Suppress("unused")
fun setupBossTrigger(trigger: Trigger) {
    trigger.enablePlayerInput(false)
        .playMusic(MusicAssets.BOSS_1, loop = true)
        .createCharacter(Character.MINOTAUR, 8.5f, 2.5f, 2f, true)
        .delay(2.5f)
        .showDialog("Boss1Dialog1")
        .createCharacter(Character.SKELETAL, 7.8f, 2f, 1f, true)
        .delay(2f)
        .showDialog("Boss1Dialog2")
        .resetAllCreatedCharacterStates()
        .enablePlayerInput(true)
        .waitForCreatedUnitsDeath()
        .enablePlayerInput(false)
        .showDialog("Boss1Dialog3")
        .playMusic(MusicAssets.FANFARE, loop = false, waitForCompletion = true)
        .playMusic(MusicAssets.LEVEL_2, true)
        .enablePlayerInput(true)
        .delay(1f)
        .enablePortal(97)
}

@Suppress("unused")
fun setupBossPitLeft(trigger: Trigger) {
    trigger.selectActivatingCharacter()
        .moveSelectedCharacterTo(3f, 2.5f)
        .damageSelectedCharacter(3f)
        .deactivateTrigger(true)
}

@Suppress("unused")
fun setupBossPitRight(trigger: Trigger) {
    trigger.selectActivatingCharacter()
        .moveSelectedCharacterTo(13f, 2.5f)
        .damageSelectedCharacter(3f)
        .deactivateTrigger(true)
}

/**
 * This trigger is used if the game is loaded after the boss fight.
 * It will activate the "Portal to Cave Top" portal and in the future
 * create the path to the next map again
 */
@Suppress("unused")
fun setupAfterBoss(trigger: Trigger) {
    trigger.conditions {
        isEntityDead(91)
    }
        .enablePortal(97)
        .deactivateTrigger(true)
}
