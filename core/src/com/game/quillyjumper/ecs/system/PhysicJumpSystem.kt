package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IntervalIteratingSystem
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.game.quillyjumper.ecs.component.CollisionComponent
import com.game.quillyjumper.ecs.component.JumpComponent
import com.game.quillyjumper.ecs.component.JumpDirection
import com.game.quillyjumper.ecs.component.PhysicComponent
import ktx.ashley.allOf
import ktx.ashley.get

private val JUMP_FORCE = Vector2(0f, 40f)

/**
 * An entity can jump when it is in contact with the ground [CollisionComponent.numGroundContacts] > 0 and
 * it is not jumping [Body.linearVelocity].y == 0f. Both checks  are necessary to avoid behavior like:
 * 1) Jumping in midair when the entity is changing from moving upwards to downwards
 * 2) Jumping again and again in a very short period of time which could then result in a "super jump" due to the different forces applied to the entity
 */
class PhysicJumpSystem : IntervalIteratingSystem(allOf(JumpComponent::class, PhysicComponent::class, CollisionComponent::class).get(), 1 / 60f) {
    override fun processEntity(entity: Entity) {
        entity[PhysicComponent.mapper]?.let { physic ->
            entity[JumpComponent.mapper]?.let { jump ->
                entity[CollisionComponent.mapper]?.let { collision ->
                    val body = physic.body
                    val jumpSpeed = body.linearVelocity.y
                    val direction = jump.direction

                    if (direction == JumpDirection.STOP) {
                        if (jumpSpeed == 0f) {
                            // entity does not want to jump and is neither jumping nor falling -> do nothing
                            return
                        }
                        // entity wants to stop the jump -> set the remaining jump steps to zero to not apply the up force
                        jump.jumpSteps = 0
                    } else if (direction == JumpDirection.JUMPING && jump.jumpSteps == 0 && collision.numGroundContacts > 0 && jumpSpeed == 0f) {
                        // entity wants to jump and jump did not start yet -> initialize jump by setting
                        // the amount of jump steps in which we want to apply an up force
                        jump.jumpSteps = jump.maxJumpSteps
                    }

                    if (jump.jumpSteps > 0) {
                        // there are still remaining steps where we need to apply the up force for the jump
                        --jump.jumpSteps
                        body.applyForceToCenter(JUMP_FORCE, true)
                        jump.direction = JumpDirection.JUMPING
                    } else {
                        // update the jump direction with the real direction by analyzing the physic body of the entity
                        jump.direction = when {
                            jumpSpeed < 0f -> JumpDirection.FALLING
                            jumpSpeed == 0f && collision.numGroundContacts > 0 && jumpSpeed == 0f -> JumpDirection.STOP
                            else -> JumpDirection.JUMPING
                        }
                    }
                }
            }
        }
    }
}