package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.min

class PhysicMoveSystem : IteratingSystem(allOf(MoveComponent::class, PhysicComponent::class).get()) {
    companion object {
        private val stopAlpha = Interpolation.pow2Out
        private val moveAlpha = Interpolation.circleOut
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val physic = entity.physicCmp
        entity.moveCmp.run {
            moveTime = min(1f, moveTime + deltaTime)

            val velocity = physic.body.linearVelocity.x
            val speed = when (order) {
                MoveOrder.NONE -> {
                    if (velocity >= -0.05f && velocity < 0.05f) {
                        // tolerance to stop entity immediately when it is below a certain speed
                        0f
                    } else {
                        stopAlpha.apply(velocity, 0f, moveTime)
                    }
                }
                MoveOrder.RIGHT -> {
                    entity.facingCmp.direction = FacingDirection.RIGHT
                    moveAlpha.apply(0f, maxSpeed, moveTime)
                }
                MoveOrder.LEFT -> {
                    entity.facingCmp.direction = FacingDirection.LEFT
                    -moveAlpha.apply(0f, maxSpeed, moveTime)
                }
            }

            // set the value of the impulse that will be applied before each physic step call
            physic.impulse.x = physic.body.mass * (speed - velocity)

            // in case the entity is rendered then flip its sprite according to the move direction
            entity[RenderComponent.mapper]?.sprite?.run {
                when {
                    speed > 0f -> setFlip(false, isFlipY)
                    speed < 0f -> setFlip(true, isFlipY)
                }
            }
        }
    }
}