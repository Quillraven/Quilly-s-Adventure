package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.math.Interpolation
import com.game.quillyjumper.ecs.component.JumpComponent
import com.game.quillyjumper.ecs.component.JumpOrder
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.execute
import ktx.ashley.allOf
import kotlin.math.min

class PhysicJumpSystem :
    IteratingSystem(allOf(JumpComponent::class, PhysicComponent::class).get()) {
    companion object {
        val jumpAlpha: Interpolation = Interpolation.pow2Out
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.execute(PhysicComponent.mapper, JumpComponent.mapper) { physic, jump ->
            // interpolate jump speed via jump time (range 0..1)
            jump.jumpTime = min(1f, jump.jumpTime + deltaTime)

            // set physic impulse y value directly which will be applied one time before world step is called
            // in the PhysicSystem
            physic.impulse.y = when {
                jump.order == JumpOrder.JUMP -> {
                    // maximum jump speed is 3.5f
                    physic.body.mass * (jumpAlpha.apply(0f, 3.5f, jump.jumpTime) - physic.body.linearVelocity.y)
                }
                else -> {
                    // stop jump -> set impulse to zero so it won't be applied anymore
                    jump.jumpTime = 0f
                    0f
                }
            }
        }
    }
}