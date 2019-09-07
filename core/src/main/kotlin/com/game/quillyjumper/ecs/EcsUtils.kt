package com.game.quillyjumper.ecs

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.fsm.State
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.FIXTURE_TYPE_FOOT_SENSOR
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.ai.DefaultState
import com.game.quillyjumper.ai.EntityAgent
import com.game.quillyjumper.configuration.CharacterCfg
import com.game.quillyjumper.configuration.ItemCfg
import com.game.quillyjumper.ecs.component.*
import com.game.quillyjumper.input.InputController
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

fun Engine.character(
    cfg: CharacterCfg,
    world: World,
    input: InputController,
    posX: Float,
    posY: Float,
    z: Int = 0,
    initialState: State<EntityAgent> = DefaultState.IDLE,
    compData: EngineEntity.() -> Unit = { Unit }
): Entity {
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
        val physic = with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.DynamicBody) {
                position.set(posX + cfg.size.x * 0.5f, posY + cfg.size.y * 0.5f)
                userData = this@entity.entity
                // characters do not need to rotate
                fixedRotation = true
                box(cfg.size.x, cfg.size.y, cfg.collBodyOffset) {
                    // characters should not stick on walls or other physic objects
                    friction = 0f
                }
                // ground sensor to detect if entity can jump
                box(cfg.size.x * 0.5f, 0.25f, PhysicComponent.tmpVec2.set(0f, -cfg.size.y * 0.5f)) {
                    userData = FIXTURE_TYPE_FOOT_SENSOR
                    this.isSensor = true
                }
            }
        }
        // move
        val move = with<MoveComponent> {
            maxSpeed = cfg.speed
        }
        // jump
        val jump = with<JumpComponent>()
        // render
        val render = with<RenderComponent>()
        // type of entity
        with<EntityTypeComponent> {
            this.type = cfg.entityType
        }
        // collision to store colliding entities
        with<CollisionComponent>()
        // animation
        val animation = with<AnimationComponent> {
            modelType = cfg.modelType
        }
        // state
        with<StateComponent> {
            if (stateMachine.owner == null) {
                // create new entity agent
                stateMachine.owner = EntityAgent(this@entity.entity, input, this, physic, animation, render, move, jump)
            } else {
                // update entity agent fields
                stateMachine.owner.apply {
                    this.entity = this@entity.entity
                    this.state = this@with
                    this.physic = physic
                    this.animation = animation
                    this.render = render
                    this.move = move
                    this.jump = jump
                }
            }
            stateMachine.setInitialState(initialState)
        }
        // optional component data via lambda parameter
        this.compData()
    }
}

fun Engine.item(cfg: ItemCfg, world: World, posX: Float, posY: Float): Entity {
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
        // portal information
        with<PortalComponent> {
            this.targetMap = targetMap
            this.targetOffsetX = targetOffsetX
            this.targetPortal = targetPortal
        }
    }
}
