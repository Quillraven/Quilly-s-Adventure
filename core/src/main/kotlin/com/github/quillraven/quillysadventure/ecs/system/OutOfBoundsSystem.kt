package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Rectangle
import com.github.quillraven.quillysadventure.ecs.component.*
import com.github.quillraven.quillysadventure.event.GameEventListener
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.math.vec2

const val ENTITY_FLAG_SAVE_POINT_NOT_ACTIVE = 0
const val ENTITY_FLAG_SAVE_POINT_ACTIVE = 1

class OutOfBoundsSystem(private val gameEventManager: GameEventManager) : MapChangeListener, GameEventListener,
    IteratingSystem(
        allOf(
            TransformComponent::class,
            EntityTypeComponent::class
        ).exclude(RemoveComponent::class).get()
    ) {
    private val boundaries = Rectangle()
    private val entityBoundingRect = Rectangle()
    private val playerCheckpointPosition = vec2()

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addGameEventListener(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeGameEventListener(this)
    }

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
            // otherwise respawn player at latest checkpoint
            if (entity.typeCmp.type == EntityType.PLAYER) {
                entity.physicCmp.body.setTransform(
                    playerCheckpointPosition.x + entityBoundingRect.width * 0.5f,
                    playerCheckpointPosition.y + entityBoundingRect.height * 0.5f,
                    0f
                )
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

    override fun activateSavepoint(savepoint: Entity) {
        playerCheckpointPosition.set(savepoint.transfCmp.position)
    }
}
