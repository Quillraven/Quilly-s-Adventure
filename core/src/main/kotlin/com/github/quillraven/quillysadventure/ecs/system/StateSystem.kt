package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.StateComponent
import com.github.quillraven.quillysadventure.ecs.component.stateCmp
import ktx.ashley.allOf
import ktx.ashley.exclude

class StateSystem : IteratingSystem(allOf(StateComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.stateCmp.run {
            stateTime += deltaTime
            stateMachine.update()
        }
    }
}
