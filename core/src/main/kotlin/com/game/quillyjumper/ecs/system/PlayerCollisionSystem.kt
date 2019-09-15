package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.map.MapManager
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.log.logger

private val LOG = logger<PlayerCollisionSystem>()

class PlayerCollisionSystem(private val mapManager: MapManager) :
    IteratingSystem(allOf(PlayerComponent::class, CollisionComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[CollisionComponent.mapper]?.let { collision ->
            // loop through all colliding entities
            collision.entities.forEach { collidingEntity ->
                if (collidingEntity[EntityTypeComponent.mapper]?.type == EntityType.PORTAL) {
                    // player collides with a portal -> move player to new location/map
                    val portalCmp = collidingEntity[PortalComponent.mapper]
                    if (portalCmp == null) {
                        LOG.error { "Portal entity is without portal component. Cannot port player." }
                        return@forEach
                    }
                    mapManager.setMap(portalCmp.targetMap, portalCmp.targetPortal, portalCmp.targetOffsetX)
                    // ignore any other collisions for that frame because the player got moved to a new map
                    return
                }
            }
        }
    }
}