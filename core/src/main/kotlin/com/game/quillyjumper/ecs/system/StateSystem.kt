package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.StateComponent
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.ashley.get

class StateSystem : IteratingSystem(allOf(StateComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[StateComponent.mapper]?.let { state ->
            state.stateTime += deltaTime
            state.stateMachine.update()
        }
    }
}