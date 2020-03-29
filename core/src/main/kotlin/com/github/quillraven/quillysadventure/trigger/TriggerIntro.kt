package com.github.quillraven.quillysadventure.trigger

import com.badlogic.gdx.graphics.g2d.Animation
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDelay
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveOrderCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayAnimationCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectCharacterByType
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSetPlayerInput
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionShowDialog

@Suppress("unused")
fun setupSceneTrigger(trigger: Trigger) {
    trigger.actions {
        action<TriggerActionSetPlayerInput> { enable = false }
        var selectAction = action<TriggerActionSelectCharacterByType> { type = Character.MINOTAUR }
        action<TriggerActionPlayAnimationCharacter> {
            mode = Animation.PlayMode.NORMAL
            type = AnimationType.ATTACK3
            waitForCompletion = true
            selectCharacterAction = selectAction
        }
        action<TriggerActionDelay> { delay = 0.5f }
        action<TriggerActionShowDialog> {
            dialogKey = "IntroDialog1"
        }
        action<TriggerActionMoveOrderCharacter> {
            selectCharacterAction = selectAction
            order = MoveOrder.RIGHT
        }
        selectAction = action { type = Character.GIRL }
        action<TriggerActionMoveOrderCharacter> {
            selectCharacterAction = selectAction
            order = MoveOrder.RIGHT
        }
        action<TriggerActionDelay> { delay = 7.5f }
        selectAction = action { type = Character.MINOTAUR }
        action<TriggerActionMoveOrderCharacter> {
            selectCharacterAction = selectAction
            order = MoveOrder.NONE
        }
        action<TriggerActionDelay> { delay = 0.8f }
        action<TriggerActionShowDialog> {
            dialogKey = "IntroDialog2"
        }
        action<TriggerActionMoveOrderCharacter> {
            selectCharacterAction = selectAction
            order = MoveOrder.RIGHT
        }
        action<TriggerActionDelay> { delay = 3f }
        action<TriggerActionSetPlayerInput> { enable = true }
    }
}
