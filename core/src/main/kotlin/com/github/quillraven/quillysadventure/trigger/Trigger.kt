package com.github.quillraven.quillysadventure.trigger

import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.github.quillraven.quillysadventure.assets.MusicAssets
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.trigger.action.*
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

    private fun lastCreatedCharacterAction(): TriggerActionCreateCharacter =
        actions.find { it is TriggerActionCreateCharacter } as TriggerActionCreateCharacter

    fun resetLastCreatedCharacterState(): Trigger {
        actions.add(ReflectionPool(TriggerActionResetState::class.java).obtain().apply {
            this.createCharacterAction = lastCreatedCharacterAction()
        })
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

    private fun lastSelectedCharacterAction(): TriggerActionSelectActivatingCharacter =
        actions.find { it is TriggerActionSelectActivatingCharacter } as TriggerActionSelectActivatingCharacter

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

    companion object {
        val pool = ReflectionPool<Trigger>(Trigger::class.java, 8)
    }
}
