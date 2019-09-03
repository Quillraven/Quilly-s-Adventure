package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.FIXTURE_TYPE_FOOT_SENSOR
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.TextureAtlasAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.configuration.CharacterCfg
import com.game.quillyjumper.configuration.ItemCfg
import com.game.quillyjumper.ecs.component.*
import ktx.ashley.entity
import ktx.box2d.body

// float array to define the vertices of a loop shape for scenery objects
private val TMP_FLOAT_ARRAY = FloatArray(8) { 0f }

fun Engine.character(cfg: CharacterCfg, world: World, posX: Float, posY: Float, z: Int = 0): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            this.z = z
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(cfg.size)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.DynamicBody) {
                position.set(posX + cfg.size.x * 0.5f, posY + cfg.size.y * 0.5f)
                userData = this@entity.entity
                box(cfg.size.x, cfg.size.y, cfg.collBodyOffset)
                // ground sensor to detect if entity can jump
                box(cfg.size.x * 0.5f, 0.25f, PhysicComponent.tmpVec2.set(0f, -cfg.size.y * 0.5f)) {
                    userData = FIXTURE_TYPE_FOOT_SENSOR
                    this.isSensor = true
                }
            }
        }
        // move
        if (cfg.speed > 0f) {
            with<MoveComponent> {
                maxSpeed = cfg.speed
            }
        }
        // jump
        with<JumpComponent>()
        // render
        with<RenderComponent>()
        // type of entity
        with<EntityTypeComponent> {
            this.type = cfg.entityType
        }
        // collision to store colliding entities
        with<CollisionComponent>()
        // animation
        with<AnimationComponent> {
            modelType = cfg.modelType
        }
        // state
        with<StateComponent>()
    }
}

fun Engine.item(cfg: ItemCfg, world: World, assets: AssetManager, posX: Float, posY: Float): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            this.z = 0
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(1f, 1f)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                position.set(posX + 0.5f, posY + 0.5f)
                userData = this@entity.entity
                box(1f, 1f) {
                    isSensor = true
                }
            }
        }
        // render
        with<RenderComponent> {
            sprite.apply {
                //TODO add safety in case region cannot be found
                // maybe create item/character cache, etc. AFTER assets are loaded because that way we could
                // set texture regions in our Cfg classes instead of strings and check for issues in those
                // classes directly
                val region = assets[TextureAtlasAssets.GAME_OBJECTS].findRegion(cfg.regionKey)
                texture = region.texture
                setRegion(region)
                // keep aspect ratio of original texture and scale it to fit into the world units
                setSize(region.regionWidth * UNIT_SCALE, region.regionHeight * UNIT_SCALE)
                setOriginCenter()
            }
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.ITEM
        }
    }
}

fun Engine.scenery(world: World, posX: Float, posY: Float, width: Float, height: Float): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            this.z = 0
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(width, height)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                position.set(posX + width * 0.5f, posY + height * 0.5f)
                userData = this@entity.entity
                // define loop vertices
                // bottom left corner
                TMP_FLOAT_ARRAY[0] = -width * 0.5f
                TMP_FLOAT_ARRAY[1] = -height * 0.5f
                // top left corner
                TMP_FLOAT_ARRAY[2] = -width * 0.5f
                TMP_FLOAT_ARRAY[3] = height * 0.5f
                // top right corner
                TMP_FLOAT_ARRAY[4] = width * 0.5f
                TMP_FLOAT_ARRAY[5] = height * 0.5f
                // bottom right corner
                TMP_FLOAT_ARRAY[6] = width * 0.5f
                TMP_FLOAT_ARRAY[7] = -height * 0.5f
                loop(TMP_FLOAT_ARRAY)
            }
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.SCENERY
        }
    }
}

