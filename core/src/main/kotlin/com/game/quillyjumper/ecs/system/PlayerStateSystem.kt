package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.PlayerComponent
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.StateComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

class PlayerStateSystem :
    IteratingSystem(allOf(PlayerComponent::class, StateComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[StateComponent.mapper]?.stateMachine?.update()
    }
}