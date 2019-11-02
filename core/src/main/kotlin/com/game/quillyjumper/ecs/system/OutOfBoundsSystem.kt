package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.math.vec2

class OutOfBoundsSystem : MapChangeListener, IteratingSystem(
    allOf(
        TransformComponent::class,
        EntityTypeComponent::class
    ).exclude(RemoveComponent::class).get()
) {
    private val boundaries = Rectangle()
    private val entityBoundingRect = Rectangle()
    private val playerCheckpointPosition = vec2()

    override fun processEntity(entity: Entity, deltaTime: Float) {
        with(entity.transfCmp) {
            entityBoundingRect.set(
                position.x,
                position.y,
                size.x,
                size.y
            )
        }
        if (!boundaries.contains(entityBoundingRect) && !boundaries.overlaps(entityBoundingRect)) {
            // entity is out of bounds
            // if it is en enemy entity then remove it
            // otherwise reduce life from player and respawn him at latest checkpoint
            if (entity.typeCmp.type == EntityType.PLAYER) {
                entity.physicCmp.body.setTransform(playerCheckpointPosition, 0f)
                with(entity.takeDamageCmp) {
                    damage = 3f
                    // TODO make a real damage source because otherwise player gets XP for killing himself
                    source = entity
                }
            } else if (entity.typeCmp.type == EntityType.ENEMY) {
                entity.add(engine.createComponent(RemoveComponent::class.java))
            }
        }
    }

    override fun mapChange(newMap: Map) {
        boundaries.run {
            x = 0f
            y = 0f
            width = newMap.width
            height = newMap.height
        }
        playerCheckpointPosition.set(newMap.startLocation)
    }
}