package com.game.quillyjumper.trigger

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.audio.AudioService
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.event.GameEventManager
import com.game.quillyjumper.getAudioService

abstract class Trigger(
    val triggerEntity: Entity,
    val gameEventManager: GameEventManager,
    val engine: Engine,
    val world: World,
    val characterCfgs: CharacterConfigurations,
    val audioService: AudioService = Gdx.app.getAudioService()
) {
    open fun update(deltaTime: Float) {}

    fun destroy() {
        triggerEntity.add(engine.createComponent(RemoveComponent::class.java))
    }

    fun cleanup() {
        gameEventManager.removeFromAllListeners(this)
    }
}
