package com.game.quillyjumper.trigger

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.AudioManager
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.ecs.component.ModelType
import com.game.quillyjumper.ecs.component.aniCmp
import com.game.quillyjumper.event.GameEventListener
import com.game.quillyjumper.event.GameEventManager

class Map2TriggerEnter(
    gameEventManager: GameEventManager,
    audioManager: AudioManager,
    engine: Engine,
    world: World
) : Trigger(gameEventManager, audioManager, engine, world) {
    init {
        // TODO show cave dialog
        println("Entering cave")
    }
}

class Map2TriggerBoss(
    gameEventManager: GameEventManager,
    audioManager: AudioManager,
    engine: Engine,
    world: World
) : Trigger(gameEventManager, audioManager, engine, world), GameEventListener {
    private var isMinotaurDead = false
    private var isSkeletalDead = false

    init {
        gameEventManager.addGameEventListener(this)
    }

    override fun playerTriggerContact(player: Entity, trigger: Entity) {
        audioManager.play(MusicAssets.BOSS_1)
    }

    override fun characterDeath(character: Entity) {
        when (character.aniCmp.modelType) {
            ModelType.MINOTAUR -> isMinotaurDead = true
            ModelType.SKELETAL -> isSkeletalDead = true
            else -> {
            }
        }

        if (isMinotaurDead && isSkeletalDead) {
            audioManager.play(MusicAssets.FANFARE)
        }
    }
}