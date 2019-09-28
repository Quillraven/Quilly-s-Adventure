package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.map.MapManager
import ktx.ashley.allOf

class PlayerCollisionSystem(private val mapManager: MapManager) :
    IteratingSystem(allOf(PlayerComponent::class, CollisionComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.collCmp.run {
            // loop through all colliding entities
            entities.forEach { collidingEntity ->
                if (collidingEntity.typeCmp.type == EntityType.PORTAL) {
                    // player collides with a portal -> move player to new location/map
                    collidingEntity.portalCmp.run { mapManager.setMap(targetMap, targetPortal, targetOffsetX) }
                    // ignore any other collisions for that frame because the player got moved to a new map
                    return
                }
            }
        }
    }
}