package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf
import kotlin.math.max
import kotlin.math.min

class PhysicMoveSystem : IteratingSystem(allOf(MoveComponent::class, PhysicComponent::class).get()) {
    private val impulse = Vector2(0f, 0f)

    override fun processEntity(entity: Entity, deltaTime: Float) {
        // calculate desired velocity
        // entity will gradually increase its speed until it reaches max speed
        val currentSpeed = entity.move.speed
        val acceleration = entity.move.acceleration * deltaTime
        val direction = entity.move.direction
        entity.move.speed = when {
            direction == MoveDirection.STOP && currentSpeed > 0 -> max(0f, currentSpeed - 2 * acceleration)
            direction == MoveDirection.STOP -> min(0f, currentSpeed + 2 * acceleration)
            direction == MoveDirection.LEFT -> max(-entity.move.maxSpeed, min(0f, currentSpeed - acceleration))
            else -> min(entity.move.maxSpeed, max(0f, currentSpeed + acceleration))
        }

        // apply impulse to physic body
        impulse.set(entity.physic.body.mass * (entity.move.speed - entity.physic.body.linearVelocity.x), 0f)
        entity.physic.body.applyLinearImpulse(impulse, entity.physic.body.worldCenter, true)
    }
}