package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.ecs.execute
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.min

class PhysicMoveSystem : IteratingSystem(allOf(MoveComponent::class, PhysicComponent::class).get()) {
    companion object {
        private val stopAlpha = Interpolation.pow2Out
        private val moveAlpha = Interpolation.circleOut
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.execute(MoveComponent.mapper, PhysicComponent.mapper) { move, physic ->
            move.moveTime = min(1f, move.moveTime + deltaTime)

            val velocity = physic.body.linearVelocity.x
            val speed = when (move.order) {
                MoveOrder.NONE -> {
                    if (velocity >= -0.05f && velocity < 0.05f) {
                        // tolerance to stop entity immediately when it is below a certain speed
                        0f
                    } else {
                        stopAlpha.apply(velocity, 0f, move.moveTime)
                    }
                }
                MoveOrder.RIGHT -> {
                    entity[FacingComponent.mapper]?.direction = FacingDirection.RIGHT
                    moveAlpha.apply(0f, move.maxSpeed, move.moveTime)
                }
                MoveOrder.LEFT -> {
                    entity[FacingComponent.mapper]?.direction = FacingDirection.LEFT
                    -moveAlpha.apply(0f, move.maxSpeed, move.moveTime)
                }
            }

            // set the value of the impulse that will be applied before each physic step call
            physic.impulse.x = physic.body.mass * (speed - velocity)

            // in case the entity is rendered then flip its sprite according to the move direction
            entity[RenderComponent.mapper]?.also { render ->
                when {
                    speed > 0f -> render.sprite.setFlip(false, render.sprite.isFlipY)
                    speed < 0f -> render.sprite.setFlip(true, render.sprite.isFlipY)
                }
            }
        }
    }
}