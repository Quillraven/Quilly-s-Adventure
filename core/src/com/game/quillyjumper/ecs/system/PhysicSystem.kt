package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.component.TransformComponent
import ktx.ashley.allOf
import ktx.ashley.get
import kotlin.math.min

class PhysicSystem(private val world: World,
                   ecsEngine: Engine,
                   private val interval: Float = 1 / 60f) : EntitySystem() {
    private var entities = ecsEngine.getEntitiesFor(allOf(PhysicComponent::class, TransformComponent::class).get())
    private var accumulator = 0f

    override fun update(deltaTime: Float) {
        accumulator += min(0.25f, deltaTime)
        while (accumulator >= interval) {
            // store position of physic bodies before the world gets updated to calculate
            // and interpolated position for the rendering.
            // This will smooth the graphics for the user and avoids "jittering"
            entities.forEach { entity ->
                entity[TransformComponent.mapper]?.let { transform ->
                    entity[PhysicComponent.mapper]?.let { physic ->
                        transform.prevPosition.set(physic.body.position)
                    }
                }
            }

            // step physic world with a constant interval between 1/45f and 1/300f
            world.step(interval, 6, 2)
            accumulator -= interval
        }

        // store position of physic bodies and calculate interpolated position for transform component
        val alpha = accumulator / interval
        entities.forEach { entity ->
            entity[TransformComponent.mapper]?.let { transform ->
                entity[PhysicComponent.mapper]?.let { physic ->
                    transform.position.set(physic.body.position)
                    transform.interpolatedPosition.set(transform.prevPosition.lerp(transform.position, alpha))
                }
            }
        }
    }
}