package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.entity
import ktx.box2d.body

fun Engine.gameObject(world: World,
                      textureRegion: TextureRegion,
                      posX: Float, posY: Float,
                      width: Float = 1f, height: Float = 1f,
                      speed: Float = 1f,
                      bodyType: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
                      collBodyOffsetX: Float = 0f, collBodyOffsetY: Float = 0f): Entity {
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
                //TODO update to newer LibKTX version once it is available to get rid of the memory leak
                box(width, height, PhysicComponent.tmpVec2.set(collBodyOffsetX, collBodyOffsetY))
            }
        }
        // move
        with<MoveComponent> {
            maxSpeed = speed
        }
        // jump
        with<JumpComponent>()
        // render
        with<RenderComponent> {
            sprite.apply {
                texture = textureRegion.texture
                setRegion(0, 0, texture.width, texture.height)
                // keep aspect ratio of original texture and scale it to fit into the world units
                setBounds(posX, posY, texture.width * UNIT_SCALE, texture.height * UNIT_SCALE)
                setOriginCenter()
            }
        }
    }
}
