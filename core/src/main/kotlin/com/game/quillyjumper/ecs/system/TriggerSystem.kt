package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.TriggerComponent
import com.game.quillyjumper.ecs.component.triggerCmp
import ktx.ashley.allOf
import ktx.ashley.exclude

class TriggerSystem : IteratingSystem(allOf(TriggerComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        with(entity.triggerCmp) {
            if (triggerUpdate) {
                trigger.update(deltaTime)
            }
        }
    }
}
