package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.math.Vector2
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.allOf

private val JUMP_FORCE = Vector2(0f, 40f)

class PhysicJumpSystem : IntervalIteratingSystem(allOf(JumpComponent::class, PhysicComponent::class).get(), 1 / 60f) {
    override fun processEntity(entity: Entity) {
        val body = entity.physic.body
        val jumpSpeed = body.linearVelocity.y
        val direction = entity.jump.direction

        if (direction == JumpDirection.STOP) {
            if (jumpSpeed == 0f) {
                // entity does not want to jump and is neither jumping nor falling -> do nothing
                return
            }
            // entity wants to stop the jump -> set the remaining jump steps to zero to not apply the up force
            entity.jump.jumpSteps = 0
        } else if (direction == JumpDirection.JUMPING && entity.jump.jumpSteps == 0 && jumpSpeed == 0f) {
            // entity wants to jump and jump did not start yet -> initialize jump by setting
            // the amount of jump steps in which we want to apply to up force
            entity.jump.jumpSteps = entity.jump.maxJumpSteps
        }

        if (entity.jump.jumpSteps > 0) {
            // there are still remaining steps where we need to apply the up force for the jump
            --entity.jump.jumpSteps
            body.applyForceToCenter(JUMP_FORCE, true)
            entity.jump.direction = JumpDirection.JUMPING
        } else {
            // update the jump direction with the real direction by analyzing the physic body of the entity
            entity.jump.direction = when {
                jumpSpeed < 0f -> JumpDirection.FALLING
                //TODO add check for ground sensor that entity is really in contact with it before stopping the jump
                // otherwise it could rejump again in midair when it reaches the top point of the jump curve
                jumpSpeed == 0f -> JumpDirection.STOP
                else -> JumpDirection.JUMPING
            }
        }
    }
}