package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.CollisionComponent
import com.game.quillyjumper.ecs.component.PlayerComponent
import ktx.ashley.allOf

//private val LOG = logger<PlayerCollisionSystem>()

class PlayerCollisionSystem : IteratingSystem(allOf(PlayerComponent::class, CollisionComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        /*entity[CollisionComponent.mapper]?.let { collision ->
            collision.entities.forEach { collidingEntity -> LOG.debug { "${collidingEntity[EntityTypeComponent.mapper]?.type}" } }
            LOG.debug { "${collision.numGroundContacts}" }
        }*/
    }
}