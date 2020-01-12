package com.game.quillyjumper.trigger

import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.configuration.Character

@Suppress("unused")
fun setupBossTrigger(trigger: Trigger) {
    trigger.enablePlayerInput(false)
        .playMusic(MusicAssets.BOSS_1, loop = true)
        .createCharacter(Character.MINOTAUR, 8.5f, 2.5f, 2f, true)
        .delay(2f)
        .resetLastCreatedCharacterState()
        .createCharacter(Character.SKELETAL, 7.8f, 2f, 1f)
        .enablePlayerInput(true)
        .waitForCreatedUnitsDeath()
        .enablePlayerInput(false)
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
