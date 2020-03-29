package com.github.quillraven.quillysadventure.trigger

import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionCreateCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDamageCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDeactivateTrigger
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDelay
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionEnablePortal
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayMusic
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionResetState
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectActivatingCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSetPlayerInput
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionShowDialog
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionWaitCreatedCharacterDeath
import com.github.quillraven.quillysadventure.trigger.condition.TriggerConditionIsEntityAlive

@Suppress("unused")
fun setupBossTrigger(trigger: Trigger) {
    trigger.actions {
        action<TriggerActionSetPlayerInput> { enable = false }
        action<TriggerActionPlayMusic> {
            loop = true
            musicType = MusicAssets.BOSS_1
            waitForMusicCompletion = false
        }
        val createAction1 = action<TriggerActionCreateCharacter> {
            type = Character.MINOTAUR
            spawnLocation.set(8.5f, 2.5f)
            fadeTime = 2f
            holdPosition = true
        }
        action<TriggerActionDelay> { delay = 2.5f }
        action<TriggerActionShowDialog> { dialogKey = "Boss1Dialog1" }
        val createAction2 = action<TriggerActionCreateCharacter> {
            type = Character.SKELETAL
            spawnLocation.set(7.8f, 2f)
            fadeTime = 1f
            holdPosition = true
        }
        action<TriggerActionDelay> { delay = 2f }
        action<TriggerActionShowDialog> { dialogKey = "Boss1Dialog2" }
        action<TriggerActionResetState> { createCharacterAction = createAction1 }
        action<TriggerActionResetState> { createCharacterAction = createAction2 }
        action<TriggerActionSetPlayerInput> { enable = true }
        action<TriggerActionWaitCreatedCharacterDeath> {}
        action<TriggerActionWaitCreatedCharacterDeath> {
            createCharacterActions.add(createAction1)
            createCharacterActions.add(createAction2)
        }
        action<TriggerActionSetPlayerInput> { enable = false }
        action<TriggerActionShowDialog> { dialogKey = "Boss1Dialog3" }
        action<TriggerActionPlayMusic> {
            loop = false
            musicType = MusicAssets.FANFARE
            waitForMusicCompletion = true
        }
        action<TriggerActionPlayMusic> {
            loop = true
            musicType = MusicAssets.LEVEL_2
            waitForMusicCompletion = false
        }
        action<TriggerActionSetPlayerInput> { enable = true }
        action<TriggerActionDelay> { delay = 1f }
        action<TriggerActionEnablePortal> { portalID = 97 }
    }
}

@Suppress("unused")
fun setupBossPitLeft(trigger: Trigger) {
    trigger.actions {
        val selectAction = action<TriggerActionSelectActivatingCharacter> { this.trigger = trigger }
        action<TriggerActionMoveCharacter> {
            selectCharacterAction = selectAction
            targetLocation.set(3f, 2.5f)
        }
        action<TriggerActionDamageCharacter> {
            selectCharacterAction = selectAction
            damage = 3f
        }
        action<TriggerActionDeactivateTrigger> {
            this.trigger = trigger
            reset = true
        }
    }
}

@Suppress("unused")
fun setupBossPitRight(trigger: Trigger) {
    trigger.actions {
        val selectAction = action<TriggerActionSelectActivatingCharacter> { this.trigger = trigger }
        action<TriggerActionMoveCharacter> {
            selectCharacterAction = selectAction
            targetLocation.set(13f, 2.5f)
        }
        action<TriggerActionDamageCharacter> {
            selectCharacterAction = selectAction
            damage = 3f
        }
        action<TriggerActionDeactivateTrigger> {
            this.trigger = trigger
            reset = true
        }
    }
}

/**
 * This trigger is used if the game is loaded after the boss fight.
 * It will activate the "Portal to Cave Top" portal and in the future
 * create the path to the next map again
 */
@Suppress("unused")
fun setupAfterBoss(trigger: Trigger) {
    trigger.conditions {
        condition<TriggerConditionIsEntityAlive> {
            checkAlive = false
            tmxMapID = 91
        }
    }.actions {
        action<TriggerActionEnablePortal> { portalID = 97 }
        action<TriggerActionDeactivateTrigger> {
            this.trigger = trigger
            reset = true
        }
    }
}
