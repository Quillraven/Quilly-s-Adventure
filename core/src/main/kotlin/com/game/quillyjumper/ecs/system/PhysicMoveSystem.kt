package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.MoveComponent
import com.game.quillyjumper.ecs.component.MoveDirection
import com.game.quillyjumper.ecs.component.PhysicComponent
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.max
import kotlin.math.min

class PhysicMoveSystem : IteratingSystem(allOf(MoveComponent::class, PhysicComponent::class).get()) {
    private val impulse = Vector2(0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[MoveComponent.mapper]?.let { move ->
            entity[PhysicComponent.mapper]?.let { physic ->
                // calculate desired velocity
                // entity will gradually increase its speed until it reaches max speed
                val currentSpeed = move.speed
                val acceleration = move.acceleration * deltaTime
                val direction = move.direction
                move.speed = when {
                    direction == MoveDirection.STOP && currentSpeed > 0 -> max(0f, currentSpeed - 2 * acceleration)
                    direction == MoveDirection.STOP -> min(0f, currentSpeed + 2 * acceleration)
                    direction == MoveDirection.LEFT -> max(-move.maxSpeed, min(0f, currentSpeed - acceleration))
                    else -> min(move.maxSpeed, max(0f, currentSpeed + acceleration))
                }

                val body = physic.body
                val bodySpeed = body.linearVelocity
                if (move.speed == 0f && bodySpeed.x == 0f) {
                    // entity does not want to move and is already still -> no need to do anything
                    return
                }

                // apply impulse to physic body
                impulse.set(body.mass * (move.speed - bodySpeed.x), 0f)
                body.applyLinearImpulse(impulse, body.worldCenter, true)
            }
        }
    }
}