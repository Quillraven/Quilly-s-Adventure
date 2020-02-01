package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.quillysadventure.ecs.component.FacingDirection
import com.github.quillraven.quillysadventure.ecs.component.MoveComponent
import com.github.quillraven.quillysadventure.ecs.component.MoveOrder
import com.github.quillraven.quillysadventure.ecs.component.PhysicComponent
import com.github.quillraven.quillysadventure.ecs.component.facingCmp
import com.github.quillraven.quillysadventure.ecs.component.moveCmp
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import ktx.ashley.allOf
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
            val facing = entity.facingCmp
            val speed = when {
                lockMovement -> {
                    moveTime = 0f
                    0f
                }
                order == MoveOrder.RIGHT -> {
                    facing.direction = FacingDirection.RIGHT
                    moveAlpha.apply(0f, maxSpeed, moveTime)
                }
                order == MoveOrder.LEFT -> {
                    facing.direction = FacingDirection.LEFT
                    -moveAlpha.apply(0f, maxSpeed, moveTime)
                }
                else -> {
                    if (velocity >= -0.05f && velocity < 0.05f) {
                        // tolerance to stop entity immediately when it is below a certain speed
                        0f
                    } else {
                        stopAlpha.apply(velocity, 0f, moveTime)
                    }
                }
            }

            // set the value of the impulse that will be applied before each physic step call
            physic.impulse.x = physic.body.mass * (speed - velocity)
        }
    }
}
