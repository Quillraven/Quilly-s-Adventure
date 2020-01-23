package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.quillysadventure.ecs.component.PhysicComponent
import com.github.quillraven.quillysadventure.ecs.component.TransformComponent
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import ktx.ashley.allOf
import kotlin.math.min

class PhysicSystem(
    private val world: World,
    ecsEngine: Engine,
    private val interval: Float = 1 / 45f
) : EntitySystem() {
    private val entities = ecsEngine.getEntitiesFor(allOf(PhysicComponent::class, TransformComponent::class).get())
    private var accumulator = 0f
    private val maxFramesToProcess = 5 * interval

    override fun update(deltaTime: Float) {
        accumulator += min(maxFramesToProcess, deltaTime)
        while (accumulator >= interval) {
            entities.forEach {
                it.physicCmp.run {
                    // store position of physic bodies before the world gets updated to calculate
                    // and interpolated position for the rendering.
                    // This will smooth the graphics for the user and avoids "stuttering"
                    val transform = it.transfCmp
                    transform.prevPosition.set(
                        body.position.x - transform.size.x * 0.5f,
                        body.position.y - transform.size.y * 0.5f
                    )

                    if (body.linearVelocity.x != 0f || !impulse.isZero) {
                        // body is moving or impulse specified --> apply it
                        body.applyLinearImpulse(impulse, body.worldCenter, true)
                    }
                }
            }

            // step physic world with a constant interval between 1/45f and 1/300f
            world.step(interval, 6, 2)
            accumulator -= interval
        }

        // store position of physic bodies and calculate interpolated position for transform component
        val alpha = accumulator / interval
        entities.forEach {
            it.physicCmp.run {
                val transform = it.transfCmp
                transform.position.set(
                    body.position.x - transform.size.x * 0.5f,
                    body.position.y - transform.size.y * 0.5f
                )
                transform.interpolatedPosition.set(transform.prevPosition.lerp(transform.position, alpha))
            }
        }
    }
}
