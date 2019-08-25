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
                when (move.direction) {
                    MoveDirection.STOP -> move.speed = if (move.speed > 0) max(0f, move.speed - 2 * move.acceleration) else min(0f, move.speed + 2 * move.acceleration)
                    MoveDirection.LEFT -> move.speed = max(-move.maxSpeed, min(0f, move.speed - move.acceleration))
                    MoveDirection.RIGHT -> move.speed = min(move.maxSpeed, max(0f, move.speed + move.acceleration))
                }

                // apply impulse to physic body
                impulse.set(physic.body.mass * (move.speed - physic.body.linearVelocity.x), 0f)
                physic.body.applyLinearImpulse(impulse, physic.body.worldCenter, true)
            }
        }
    }
}