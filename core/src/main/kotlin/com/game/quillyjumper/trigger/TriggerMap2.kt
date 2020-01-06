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
        .enablePortal(97)
        .enablePlayerInput(false)
        .playMusic(MusicAssets.FANFARE, loop = false, waitForCompletion = true)
        .playMusic(MusicAssets.LEVEL_2, true)
        .enablePlayerInput(true)
}
