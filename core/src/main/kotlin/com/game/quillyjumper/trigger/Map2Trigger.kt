package com.game.quillyjumper.trigger

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ai.DefaultState
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.findPortal
import com.game.quillyjumper.event.GameEventListener
import com.game.quillyjumper.event.GameEventManager

@Suppress("unused")
class Map2TriggerEnter(
    triggerEntity: Entity,
    gameEventManager: GameEventManager,
    engine: Engine,
    world: World,
    characterCfgs: CharacterConfigurations
) : Trigger(triggerEntity, gameEventManager, engine, world, characterCfgs) {
    init {
        // TODO show cave dialog
        println("Entering cave")
        destroy()
    }
}

@Suppress("unused")
class Map2TriggerBoss(
    triggerEntity: Entity,
    gameEventManager: GameEventManager,
    engine: Engine,
    world: World,
    characterCfgs: CharacterConfigurations
) : Trigger(triggerEntity, gameEventManager, engine, world, characterCfgs), GameEventListener,
    Music.OnCompletionListener {
    private lateinit var minotaur: Entity
    private lateinit var skeletal: Entity
    private var killCounter = -1
    private var elapsedTime = 0f

    init {
        gameEventManager.addGameEventListener(this)
    }

    override fun playerTriggerContact(player: Entity, trigger: Entity) {
        if (trigger != triggerEntity) return

        audioService.play(MusicAssets.BOSS_1)
        killCounter = 0
        minotaur = engine.character(
            characterCfgs[Character.MINOTAUR],
            world,
            8f,
            2.5f
        ) { with<FadeInComponent> { maxFadeTime = 2f } }

        // disable player input until boss dialog is over
        gameEventManager.disablePlayerInput()
        minotaur.stateCmp.stateMachine.changeState(DefaultState.NONE)
        trigger.triggerCmp.triggerUpdate = true
    }

    override fun update(deltaTime: Float) {
        elapsedTime += deltaTime
        if (elapsedTime >= 2f) {
            // delay actual trigger stuff until minotaur is faded in
            gameEventManager.enablePlayerInput()
            triggerEntity.triggerCmp.triggerUpdate = false
            with(minotaur.stateCmp.stateMachine) {
                changeState(previousState)
            }
            skeletal = engine.character(
                characterCfgs[Character.SKELETAL],
                world,
                8f,
                2.3f
            ) { with<FadeInComponent> { maxFadeTime = 2f } }
        }
    }

    override fun characterDeath(character: Entity) {
        if (killCounter >= 0 && (character == minotaur || character == skeletal)) {
            ++killCounter
            if (killCounter >= 2) {
                engine.findPortal(97) { portal ->
                    portal.portalCmp.active = true
                    portal.add(engine.createComponent(ParticleComponent::class.java).apply {
                        type = ParticleAssets.PORTAL2
                        offsetX = -0.5f
                    })
                }
                gameEventManager.disablePlayerInput()
                audioService.play(MusicAssets.FANFARE, false, this)
            }
        }
    }

    override fun onCompletion(music: Music) {
        // fanfare finished -> create portal to port back to top of cave and do amazing stuff
        gameEventManager.enablePlayerInput()
        audioService.play(MusicAssets.LEVEL_2)
        destroy()
    }
}
