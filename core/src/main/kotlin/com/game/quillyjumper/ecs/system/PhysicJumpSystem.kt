package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.game.quillyjumper.ecs.component.*
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
            // interpolate jump speed via jump time (range 0..1)
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
                    0f
                }
            }
        }
    }
}