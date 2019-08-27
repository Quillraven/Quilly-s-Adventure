package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.FIXTURE_TYPE_FOOT_SENSOR
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.entity
import ktx.box2d.body

fun Engine.gameObject(type: EntityType,
                      world: World,
                      posX: Float, posY: Float,
                      width: Float = 1f, height: Float = 1f,
                      textureRegion: TextureRegion? = null,
                      speed: Float = 0f,
                      bodyType: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
                      isSensor: Boolean = false,
                      collBodyOffsetX: Float = 0f, collBodyOffsetY: Float = 0f,
                      createCharacterSensors: Boolean = false): Entity {
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
                // collision body
                box(width, height, PhysicComponent.tmpVec2.set(collBodyOffsetX, collBodyOffsetY)) {
                    this.isSensor = isSensor
                }
                if (createCharacterSensors) {
                    // ground sensor to detect if entity can jump
                    box(width * 0.5f, 0.25f, Vector2(width * 0.25f, -height * 0.5f)) {
                        userData = FIXTURE_TYPE_FOOT_SENSOR
                        this.isSensor = true
                    }
                }
            }
        }
        if (speed > 0) {
            // move
            with<MoveComponent> {
                maxSpeed = speed
            }
        }
        // jump
        with<JumpComponent>()
        if (textureRegion != null) {
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
        // type of entity
        with<EntityTypeComponent> {
            this.type = type
        }
        // collision to store colliding entities
        with<CollisionComponent>()
    }
}
