package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.FIXTURE_TYPE_FOOT_SENSOR
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.entity
import ktx.box2d.body

fun Engine.gameObject(
    type: EntityType,
    world: World,
    posX: Float, posY: Float, z: Int = 0,
    width: Float = 1f, height: Float = 1f,
    textureRegion: TextureRegion? = null,
    speed: Float = 0f,
    bodyType: BodyDef.BodyType = BodyDef.BodyType.DynamicBody,
    isSensor: Boolean = false,
    collBodyOffsetX: Float = 0f, collBodyOffsetY: Float = 0f,
    createCharacterSensors: Boolean = false
): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            this.z = z
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
                    box(width * 0.5f, 0.25f, PhysicComponent.tmpVec2.set(width * 0.25f, -height * 0.5f)) {
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
        // render
        with<RenderComponent>()
        // type of entity
        with<EntityTypeComponent> {
            this.type = type
        }
        // collision to store colliding entities
        with<CollisionComponent>()
        // animation
        with<AnimationComponent>()
        // state
        with<StateComponent>()
    }
}
