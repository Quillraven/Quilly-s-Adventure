package com.github.quillraven.quillysadventure.trigger

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.ecs.component.AnimationType
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.trigger.action.TriggerAction
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionCreateCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDamageCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDeactivateTrigger
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionDelay
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionEnablePortal
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionMoveOrderCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayAnimationCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionPlayMusic
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionResetState
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectActivatingCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectCharacter
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSelectCharacterByType
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionSetPlayerInput
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionShowDialog
import com.github.quillraven.quillysadventure.trigger.action.TriggerActionWaitCreatedCharacterDeath
import ktx.collections.iterate

class Trigger : Pool.Poolable {
    private val actions = Array<TriggerAction>(8)
    private var currentIdx = 0
    lateinit var activatingCharacter: Entity
    var active = true

    fun update(deltaTime: Float): Boolean {
        while (active && currentIdx < actions.size && actions[currentIdx].update(deltaTime)) {
            if (active) ++currentIdx
        }

        return currentIdx >= actions.size
    }

    override fun reset() {
        active = true
        currentIdx = 0
        actions.iterate { action, iterator ->
            ReflectionPool(action.javaClass).free(action)
            iterator.remove()
        }
    }

    fun playMusic(type: MusicAssets, loop: Boolean = false, waitForCompletion: Boolean = false): Trigger {
        actions.add(ReflectionPool(TriggerActionPlayMusic::class.java).obtain().apply {
            this.musicType = type
            this.loop = loop
            this.waitForMusicCompletion = waitForCompletion
        })
        return this
    }

    fun createCharacter(
        type: Character,
        spawnX: Float,
        spawnY: Float,
        fadeTime: Float = 0f,
        holdPosition: Boolean = false
    ): Trigger {
        actions.add(ReflectionPool(TriggerActionCreateCharacter::class.java).obtain().apply {
            this.type = type
            this.spawnLocation.set(spawnX, spawnY)
            this.fadeTime = fadeTime
            this.holdPosition = holdPosition
        })
        return this
    }

    fun enablePlayerInput(enable: Boolean): Trigger {
        actions.add(ReflectionPool(TriggerActionSetPlayerInput::class.java).obtain().apply {
            this.enable = enable
        })
        return this
    }

    fun delay(delay: Float): Trigger {
        actions.add(ReflectionPool(TriggerActionDelay::class.java).obtain().apply {
            this.delay = delay
        })
        return this
    }

    fun resetAllCreatedCharacterStates(): Trigger {
        actions.forEach {
            if (it is TriggerActionCreateCharacter) {
                actions.add(ReflectionPool(TriggerActionResetState::class.java).obtain().apply {
                    this.createCharacterAction = it
                })
            }
        }

        return this
    }

    fun enablePortal(portalID: Int): Trigger {
        actions.add(ReflectionPool(TriggerActionEnablePortal::class.java).obtain().apply {
            this.portalID = portalID
        })
        return this
    }

    fun waitForCreatedUnitsDeath(): Trigger {
        actions.add(ReflectionPool(TriggerActionWaitCreatedCharacterDeath::class.java).obtain().apply {
            actions.forEach {
                if (it is TriggerActionCreateCharacter) {
                    createCharacterAction.add(it)
                }
            }
        })
        return this
    }

    fun selectActivatingCharacter(): Trigger {
        actions.add(ReflectionPool(TriggerActionSelectActivatingCharacter::class.java).obtain().apply {
            trigger = this@Trigger
        })
        return this
    }

    fun selectCharacterByType(type: Character): Trigger {
        actions.add(ReflectionPool(TriggerActionSelectCharacterByType::class.java).obtain().apply {
            this.type = type
        })
        return this
    }

    private fun lastSelectedCharacterAction(): TriggerActionSelectCharacter =
        actions.findLast { it is TriggerActionSelectCharacter } as TriggerActionSelectCharacter

    fun moveSelectedCharacterTo(x: Float, y: Float): Trigger {
        actions.add(ReflectionPool(TriggerActionMoveCharacter::class.java).obtain().apply {
            selectCharacterAction = lastSelectedCharacterAction()
            targetLocation.set(x, y)
        })
        return this
    }

    fun damageSelectedCharacter(damage: Float): Trigger {
        actions.add(ReflectionPool(TriggerActionDamageCharacter::class.java).obtain().apply {
            selectCharacterAction = lastSelectedCharacterAction()
            this.damage = damage
        })
        return this
    }

    fun deactivateTrigger(reset: Boolean): Trigger {
        actions.add(ReflectionPool(TriggerActionDeactivateTrigger::class.java).obtain().apply {
            trigger = this@Trigger
            this.reset = reset
        })
        return this
    }

    fun resetActions() {
        currentIdx = 0
    }

    fun showDialog(dialogKey: String): Trigger {
        actions.add(ReflectionPool(TriggerActionShowDialog::class.java).obtain().apply {
            this.dialogKey = dialogKey
        })
        return this
    }

    fun orderMoveSelecedCharacter(moveOrder: MoveOrder): Trigger {
        actions.add(ReflectionPool(TriggerActionMoveOrderCharacter::class.java).obtain().apply {
            selectCharacterAction = lastSelectedCharacterAction()
            order = moveOrder
        })
        return this
    }

    fun playAnimationSelectedCharacter(
        animationType: AnimationType,
        playMode: Animation.PlayMode,
        waitForCompletion: Boolean
    ): Trigger {
        actions.add(ReflectionPool(TriggerActionPlayAnimationCharacter::class.java).obtain().apply {
            selectCharacterAction = lastSelectedCharacterAction()
            this.mode = playMode
            type = animationType
            this.waitForCompletion = waitForCompletion
        })
        return this
    }

    companion object {
        val pool = ReflectionPool(Trigger::class.java, 8)
    }
}
