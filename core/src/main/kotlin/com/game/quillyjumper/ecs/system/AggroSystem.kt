package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf

class AggroSystem :
    IteratingSystem(allOf(AggroComponent::class, TransformComponent::class, MoveComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val entities = entity.aggroCmp.aggroEntities
        if (entities.size > 0) {
            // entities are within aggro range -> move towards the first entity
            val firstEntity = entities[0]
            if (firstEntity.transfCmp.position.x < entity.transfCmp.position.x) {
                // aggro entity is on the left side
                entity.moveCmp.order = MoveOrder.LEFT
            } else {
                // aggro entity is on the right side
                entity.moveCmp.order = MoveOrder.RIGHT
            }
            // TODO if entity within attack range then give attack order
        } else {
            // no entities in range -> do nothing
            entity.moveCmp.order = MoveOrder.NONE
        }
    }
}