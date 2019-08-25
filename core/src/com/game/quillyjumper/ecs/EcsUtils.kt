package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.ecs.component.MoveComponent
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.component.RenderComponent
import com.game.quillyjumper.ecs.component.TransformComponent
import ktx.ashley.entity
import ktx.box2d.body

fun Engine.gameObject(world: World, posX: Float, posY: Float, width: Float = 1f, height: Float = 1f, speed: Float = 1f, bodyType: BodyDef.BodyType = BodyDef.BodyType.DynamicBody): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(width, height)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(bodyType) {
                position.set(posX + width * 0.5f, posY + height * 0.5f)
                userData = this@entity.entity
                box(width, height)
            }
        }
        // move
        with<MoveComponent> {
            maxSpeed = speed
        }
        // render
        with<RenderComponent> {
            //TODO add textureAtlasRegionKey to parameters and set sprite texture accordingly
        }
    }
}