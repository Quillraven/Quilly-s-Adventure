package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.StateComponent
import com.game.quillyjumper.ecs.component.stateCmp
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