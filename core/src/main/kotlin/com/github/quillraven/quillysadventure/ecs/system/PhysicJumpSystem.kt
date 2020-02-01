package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.github.quillraven.quillysadventure.ecs.component.JumpComponent
import com.github.quillraven.quillysadventure.ecs.component.JumpOrder
import com.github.quillraven.quillysadventure.ecs.component.PhysicComponent
import com.github.quillraven.quillysadventure.ecs.component.jumpCmp
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import ktx.ashley.allOf
import kotlin.math.min

class PhysicJumpSystem :
    IteratingSystem(allOf(JumpComponent::class, PhysicComponent::class).get()) {
    companion object {
        val jumpAlpha: Interpolation = Interpolation.pow2Out
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        val physic = entity.physicCmp
        entity.jumpCmp.run {
            // interpolate jump speed via jump time (range 0..1) over 1 second
            jumpTime = min(1f, jumpTime + deltaTime)

            // set physic impulse y value directly which will be applied one time before world step is called
            // in the PhysicSystem
            physic.impulse.y = when (order) {
                JumpOrder.JUMP -> {
                    // maximum jump speed is 3.5f
                    physic.body.mass * (jumpAlpha.apply(0f, 3.5f, jumpTime) - physic.body.linearVelocity.y)
                }
                else -> {
                    // stop jump -> set impulse to zero so it won't be applied anymore
                    jumpTime = 0f
                    if (physic.body.linearVelocity.y > 0f) {
                        // if the player should not jump but still gets an upwards force then apply an impulse
                        // to stop the upwards movement.
                        // This is used to make the player stick to the ground when moving slopes upwards.
                        physic.body.mass * -physic.body.linearVelocity.y
                    } else {
                        0f
                    }
                }
            }
        }
    }
}
