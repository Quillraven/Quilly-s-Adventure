package com.github.quillraven.quillysadventure.trigger

import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder

@Suppress("unused")
fun setupSceneTrigger(trigger: Trigger) {
    trigger.enablePlayerInput(false)
        .selectCharacterByType(Character.MINOTAUR)
        .playAnimationSelectedCharacter(AnimationType.ATTACK3, Animation.PlayMode.NORMAL, true)
        .delay(0.5f)
        .showDialog("IntroDialog1")
        .orderMoveSelecedCharacter(MoveOrder.RIGHT)
        .selectCharacterByType(Character.GIRL)
        .orderMoveSelecedCharacter(MoveOrder.RIGHT)
        .delay(7.35f)
        .selectCharacterByType(Character.MINOTAUR)
        .orderMoveSelecedCharacter(MoveOrder.NONE)
        .playAnimationSelectedCharacter(AnimationType.IDLE, Animation.PlayMode.LOOP, true)
        .delay(0.75f)
        .showDialog("IntroDialog2")
        .orderMoveSelecedCharacter(MoveOrder.RIGHT)
        .delay(3f)
        .enablePlayerInput(true)
}
