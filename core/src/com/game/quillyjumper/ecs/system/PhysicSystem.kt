package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.EntitySystem
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.component.TransformComponent
import com.game.quillyjumper.ecs.component.physic
import com.game.quillyjumper.ecs.component.transform
import ktx.ashley.allOf
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
            // This will smooth the graphics for the user and avoids "stuttering"
            entities.forEach { it.transform.prevPosition.set(it.physic.body.position.x - it.transform.size.x * 0.5f, it.physic.body.position.y - it.transform.size.y * 0.5f) }

            // step physic world with a constant interval between 1/45f and 1/300f
            world.step(interval, 6, 2)
            accumulator -= interval
        }

        // store position of physic bodies and calculate interpolated position for transform component
        val alpha = accumulator / interval
        entities.forEach { entity ->
            entity.transform.position.set(entity.physic.body.position.x - entity.transform.size.x * 0.5f, entity.physic.body.position.y - entity.transform.size.y * 0.5f)
            entity.transform.interpolatedPosition.set(entity.transform.prevPosition.lerp(entity.transform.position, alpha))
        }
    }
}