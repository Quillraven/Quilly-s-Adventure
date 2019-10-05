package com.game.quillyjumper.ecs

import box2dLight.DirectionalLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.StringBuilder
import com.game.quillyjumper.FIXTURE_TYPE_AGGRO_SENSOR
import com.game.quillyjumper.FIXTURE_TYPE_FOOT_SENSOR
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ai.DefaultState
import com.game.quillyjumper.assets.ParticleAssets
import com.game.quillyjumper.configuration.CharacterCfg
import com.game.quillyjumper.configuration.ItemCfg
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.map.MapType
import ktx.ashley.EngineEntity
import ktx.ashley.entity
import ktx.box2d.BodyDefinition
import ktx.box2d.FixtureDefinition
import ktx.box2d.body
import ktx.log.logger

// float array to define the vertices of a loop shape for rectangle scenery objects
private val TMP_FLOAT_ARRAY = FloatArray(8) { 0f }
private val LOG = logger<Engine>()

// helper function to check if an entity is removed. This is needed for box2d contact listener because
// remove contact is triggered for entities that get removed and they should be ignored for the contacts events
fun Entity.isRemoved() = this.components.size() == 0

fun Engine.floatingText(
    posX: Float,
    posY: Float,
    font: BitmapFont,
    text: StringBuilder,
    color: Color,
    speedX: Float,
    speedY: Float,
    lifeSpan: Float
): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            this.position.set(posX, posY)
        }
        // text
        with<FloatingTextComponent> {
            stringBuilder.append(text)
            speed.set(speedX, speedY)
            this.font = font
            this.lifeSpan = lifeSpan
            this.color = color
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.OTHER
        }
    }
}

fun Engine.character(
    cfg: CharacterCfg,
    world: World,
    posX: Float,
    posY: Float,
    z: Int = 0,
    compData: EngineEntity.() -> Unit = { Unit }
): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX - cfg.size.x * 0.5f, posY)
            this.z = z
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(cfg.size)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.DynamicBody) {
                position.set(posX, posY + cfg.size.y * 0.5f)
                userData = this@entity.entity
                // characters do not need to rotate
                fixedRotation = true
                // characters have a ground fixture with a high friction to avoid sliding
                box(cfg.size.x * 0.8f, cfg.size.y, cfg.collBodyOffset) {
                    friction = 1f
                }
                // In addition they have two additional fixtures on the right and left side
                // with no friction to avoid sticking to walls
                box(
                    cfg.size.x * 0.1f,
                    cfg.size.y,
                    PhysicComponent.tmpVec2.set(
                        cfg.collBodyOffset.x - cfg.size.x * 0.5f + cfg.size.x * 0.1f * 0.5f,
                        cfg.collBodyOffset.y
                    )
                ) {
                    friction = 0f
                }
                box(
                    cfg.size.x * 0.1f,
                    cfg.size.y,
                    PhysicComponent.tmpVec2.set(
                        cfg.collBodyOffset.x + cfg.size.x * 0.5f - cfg.size.x * 0.1f * 0.5f,
                        cfg.collBodyOffset.y
                    )
                ) {
                    friction = 0f
                }

                // characters are not allowed to sleep because otherwise the damage emitter
                // sensors do not detect collision correctly (body needs to be awake to trigger
                // contact events)
                allowSleep = false

                // ground sensor to detect if entity can jump
                box(
                    cfg.size.x * 0.5f,
                    0.25f,
                    PhysicComponent.tmpVec2.set(0f + cfg.collBodyOffset.x, -cfg.size.y * 0.5f + cfg.collBodyOffset.y)
                ) {
                    userData = FIXTURE_TYPE_FOOT_SENSOR
                    this.isSensor = true
                }

                if (cfg.aggroRange > 0f) {
                    // character needs a sensor to detect if entities come within aggro range
                    circle(cfg.aggroRange) {
                        userData = FIXTURE_TYPE_AGGRO_SENSOR
                        isSensor = true
                    }
                }
            }
        }
        if (cfg.speed > 0f) {
            // move
            with<MoveComponent> {
                maxSpeed = cfg.speed
            }
        }
        // facing
        with<FacingComponent>()
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
        // attack
        if (cfg.attackRange > 0f) {
            with<AttackComponent> {
                range = cfg.attackRange
                cooldown = cfg.attackCooldown
            }
        }
        // stats
        if (cfg.life > 0) {
            with<StatsComponent> {
                this.armor = cfg.armor
                this.damage = cfg.damage
                this.life = cfg.life
            }
        }
        // aggro
        if (cfg.aggroRange > 0f) {
            with<AggroComponent>()
        }

        // state
        if (cfg.defaultState != DefaultState.NONE) {
            with<StateComponent> {
                stateMachine.owner = this@entity.entity
                stateMachine.setInitialState(cfg.defaultState)
            }
        }
        // optional component data via lambda parameter
        this.compData()
    }
}

fun Engine.item(cfg: ItemCfg, world: World, posX: Float, posY: Float): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX - 0.5f, posY)
            this.z = 0
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(1f, 1f)
        }
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                position.set(posX, posY + 0.5f)
                userData = this@entity.entity
                // items do not need to rotate
                fixedRotation = true
                box(1f, 1f) {
                    isSensor = true
                }
            }
        }
        // render
        with<RenderComponent> {
            sprite.apply {
                val region = cfg.region
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

private fun BodyDefinition.shape2D(shape: Shape2D, init: FixtureDefinition.() -> Unit = { Unit }) {
    // position and fixture scaled according to world units
    when (shape) {
        is Rectangle -> {
            val width = shape.width * UNIT_SCALE
            val height = shape.height * UNIT_SCALE
            position.set(shape.x * UNIT_SCALE + width * 0.5f, shape.y * UNIT_SCALE + height * 0.5f)
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
            loop(TMP_FLOAT_ARRAY).init()
        }
        is Polyline -> {
            val x = shape.x
            val y = shape.y
            position.set(x * UNIT_SCALE, y * UNIT_SCALE)
            // transformed vertices also adds the position to each
            // vertex. Therefore, we need to set position first to ZERO
            // and then restore it afterwards
            shape.setPosition(0f, 0f)
            shape.setScale(UNIT_SCALE, UNIT_SCALE)
            chain(shape.transformedVertices).init()
            // restore position
            shape.setPosition(x, y)
        }
        is Polygon -> {
            val x = shape.x
            val y = shape.y
            position.set(x * UNIT_SCALE, y * UNIT_SCALE)
            // transformed vertices also adds the position to each
            // vertex. Therefore, we need to set position first to ZERO
            // and then restore it afterwards
            shape.setPosition(0f, 0f)
            shape.setScale(UNIT_SCALE, UNIT_SCALE)
            loop(shape.transformedVertices).init()
            // restore position
            shape.setPosition(x, y)
        }
        else -> {
            LOG.error { "Unsupported shape ${shape::class.java} for scenery object." }
        }
    }
}

fun Engine.scenery(world: World, shape: Shape2D): Entity {
    return this.entity {
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                userData = this@entity.entity
                // scenery does not need to rotate
                fixedRotation = true
                // create fixture according to given shape
                // fixture gets scaled to world units
                shape2D(shape)
            }
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.SCENERY
        }
    }
}

fun Engine.portal(world: World, shape: Shape2D, targetMap: MapType, targetPortal: Int, targetOffsetX: Int): Entity {
    return this.entity {
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                userData = this@entity.entity
                // scenery does not need to rotate
                fixedRotation = true
                // create fixture according to given shape
                // fixture gets scaled to world units
                shape2D(shape) { isSensor = true }
            }
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.PORTAL
        }
        // transform
        val pos = this.entity.physicCmp.body.position
        with<TransformComponent> {
            position.set(pos.x, pos.y)
            prevPosition.set(position)
            interpolatedPosition.set(position)
        }
        // particle effect
        with<ParticleComponent> { type = ParticleAssets.PORTAL }
        // portal information
        with<PortalComponent> {
            this.targetMap = targetMap
            this.targetOffsetX = targetOffsetX
            this.targetPortal = targetPortal
        }
    }
}

fun Engine.damageEmitter(
    world: World,
    posX: Float,
    posY: Float,
    sizeX: Float,
    sizeY: Float,
    damage: Float,
    lifeSpan: Float,
    source: Entity
): Entity {
    return this.entity {
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.StaticBody) {
                position.set(posX + sizeX * 0.5f, posY + sizeY * 0.5f)
                userData = this@entity.entity
                // damage emitters do not need to rotate
                fixedRotation = true
                box(sizeX, sizeY) {
                    isSensor = true
                }
            }
        }
        // collision
        with<CollisionComponent>()
        // type
        with<EntityTypeComponent> { type = EntityType.DAMAGE_EMITTER }
        // damage
        with<DamageComponent> {
            this.damage = damage
            this.lifeSpan = lifeSpan
            this.source = source
        }
    }
}

fun Engine.globalLight(
    rayHandler: RayHandler,
    ambientColor: Color,
    shadowColor: Color,
    shadowAngle: Float
): Entity {
    // set ambient light in LightSystem
    rayHandler.setBlurNum(3)
    rayHandler.setAmbientLight(ambientColor)

    // create global light entity
    return this.entity {
        // light
        with<LightComponent> { light = DirectionalLight(rayHandler, 512, shadowColor, shadowAngle) }
        // type
        with<EntityTypeComponent> { type = EntityType.OTHER }
    }
}

fun Engine.particleEffect(
    posX: Float,
    posY: Float,
    type: ParticleAssets
): Entity {
    return this.entity {
        // transform
        with<TransformComponent> {
            position.set(posX, posY)
            prevPosition.set(position)
            interpolatedPosition.set(position)
        }
        // particle effect
        with<ParticleComponent> { this.type = type }
        // type
        with<EntityTypeComponent> { this.type = EntityType.OTHER }
    }
}