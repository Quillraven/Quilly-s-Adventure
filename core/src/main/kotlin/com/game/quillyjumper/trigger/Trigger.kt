package com.game.quillyjumper.trigger

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.ReflectionPool
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.trigger.action.*
import ktx.collections.iterate

class Trigger : Pool.Poolable {
    private val actions = Array<TriggerAction>(8)
    private var currentIdx = 0
    var active = true

    fun update(deltaTime: Float): Boolean {
        while (currentIdx < actions.size && actions[currentIdx].update(deltaTime)) {
            ++currentIdx
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

    private fun playMusic(type: MusicAssets, loop: Boolean = false, waitForCompletion: Boolean = false): Trigger {
        actions.add(ReflectionPool(TriggerActionPlayMusic::class.java).obtain().apply {
            this.musicType = type
            this.loop = loop
            this.waitForMusicCompletion = waitForCompletion
        })
        return this
    }

    private fun createCharacter(
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

    private fun enablePlayerInput(enable: Boolean): Trigger {
        actions.add(ReflectionPool(TriggerActionSetPlayerInput::class.java).obtain().apply {
            this.enable = enable
        })
        return this
    }

    private fun delay(delay: Float): Trigger {
        actions.add(ReflectionPool(TriggerActionDelay::class.java).obtain().apply {
            this.delay = delay
        })
        return this
    }

    private fun lastCreatedCharacterTrigger(): TriggerActionCreateCharacter =
        actions.find { it is TriggerActionCreateCharacter } as TriggerActionCreateCharacter

    private fun resetLastCreatedCharacterState(): Trigger {
        actions.add(ReflectionPool(TriggerActionResetState::class.java).obtain().apply {
            this.createCharTrigger = lastCreatedCharacterTrigger()
        })
        return this
    }

    private fun enablePortal(portalID: Int): Trigger {
        actions.add(ReflectionPool(TriggerActionEnablePortal::class.java).obtain().apply {
            this.portalID = portalID
        })
        return this
    }

    private fun waitForCreatedUnitsDeath(): Trigger {
        actions.add(ReflectionPool(TriggerActionWaitCreatedCharacterDeath::class.java).obtain().apply {
            actions.forEach {
                if (it is TriggerActionCreateCharacter) {
                    createdCharTriggers.add(it)
                }
            }
        })
        return this
    }

    @Suppress("unused")
    fun map2BossTriggerSetup() {
        enablePlayerInput(false)
            .playMusic(MusicAssets.BOSS_1, loop = true)
            .createCharacter(Character.MINOTAUR, 8f, 2.5f, 2f, true)
            .delay(2f)
            .resetLastCreatedCharacterState()
            .createCharacter(Character.SKELETAL, 8f, 2.3f, 2f)
            .enablePlayerInput(true)
            .waitForCreatedUnitsDeath()
            .enablePortal(97)
            .enablePlayerInput(false)
            .playMusic(MusicAssets.FANFARE, loop = false, waitForCompletion = true)
            .playMusic(MusicAssets.LEVEL_2, true)
            .enablePlayerInput(true)
    }

    companion object {
        val pool = ReflectionPool<Trigger>(Trigger::class.java, 8)
    }
}
