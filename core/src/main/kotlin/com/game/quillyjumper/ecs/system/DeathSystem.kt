package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.StatsComponent
import com.game.quillyjumper.ecs.component.statsCmp
import ktx.ashley.allOf
import ktx.ashley.exclude

class DeathSystem : IteratingSystem(allOf(StatsComponent::class).exclude(RemoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        if (entity.statsCmp.life <= 0f) {
            // entity dies because it has no more life
            entity.add(engine.createComponent(RemoveComponent::class.java))
        }
    }
}