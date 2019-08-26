package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf

private val JUMP_UP_FORCE = Vector2(0f, 20f)
private val JUMP_DOWN_FORCE = Vector2(0f, -10f)

class PhysicJumpSystem : IteratingSystem(allOf(JumpComponent::class, PhysicComponent::class).get()) {
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val body = entity.physic.body

        // increase jump timer because a jump is limited by the time an entity is in the air
        entity.jump.jumpTimer += deltaTime

        if (entity.jump.direction == JumpDirection.JUMPING && entity.jump.jumpTimer < entity.jump.maxJumpTimer) {
            // entity wants to jump and did not reach the maximum jump time time -> apply up force
            body.applyForceToCenter(JUMP_UP_FORCE, true)
        } else {
            // entity either wants to stop jumping or reached the maximum jump time -> let it fall until it reaches the ground again
            val verticalSpeed = body.linearVelocity.y
            entity.jump.direction = when {
                verticalSpeed < 0f -> JumpDirection.FALLING
                //TODO also add check that ground sensor is touching the ground again
                verticalSpeed == 0f -> {
                    // entity touched the ground -> reset the timer so that it can jump again
                    entity.jump.jumpTimer = 0f
                    JumpDirection.STOP
                }
                else -> {
                    // entity is still moving upwards --> apply down force to stop the jump
                    body.applyForceToCenter(JUMP_DOWN_FORCE, true)
                    JumpDirection.JUMPING
                }
            }
        }
    }
}