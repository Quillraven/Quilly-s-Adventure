package com.github.quillraven.quillysadventure.ecs

import box2dLight.DirectionalLight
import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.StringBuilder
import com.github.quillraven.quillysadventure.FILTER_CATEGORY_GAME_OBJECT
import com.github.quillraven.quillysadventure.FILTER_CATEGORY_ITEM
import com.github.quillraven.quillysadventure.FILTER_CATEGORY_SCENERY
import com.github.quillraven.quillysadventure.FIXTURE_TYPE_AGGRO_SENSOR
import com.github.quillraven.quillysadventure.FIXTURE_TYPE_FOOT_SENSOR
import com.github.quillraven.quillysadventure.UNIT_SCALE
import com.github.quillraven.quillysadventure.ai.DefaultGlobalState
import com.github.quillraven.quillysadventure.ai.DefaultState
import com.github.quillraven.quillysadventure.assets.ParticleAssets
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.configuration.CharacterCfg
import com.github.quillraven.quillysadventure.configuration.ItemCfg
import com.github.quillraven.quillysadventure.ecs.component.AbilityComponent
import com.github.quillraven.quillysadventure.ecs.component.AggroComponent
import com.github.quillraven.quillysadventure.ecs.component.AnimationComponent
import com.github.quillraven.quillysadventure.ecs.component.AttackComponent
import com.github.quillraven.quillysadventure.ecs.component.CharacterTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.CollisionComponent
import com.github.quillraven.quillysadventure.ecs.component.DealDamageComponent
import com.github.quillraven.quillysadventure.ecs.component.EntityType
import com.github.quillraven.quillysadventure.ecs.component.EntityTypeComponent
import com.github.quillraven.quillysadventure.ecs.component.FacingComponent
import com.github.quillraven.quillysadventure.ecs.component.FloatingTextComponent
import com.github.quillraven.quillysadventure.ecs.component.JumpComponent
import com.github.quillraven.quillysadventure.ecs.component.LightComponent
import com.github.quillraven.quillysadventure.ecs.component.MoveComponent
import com.github.quillraven.quillysadventure.ecs.component.ParticleComponent
import com.github.quillraven.quillysadventure.ecs.component.PhysicComponent
import com.github.quillraven.quillysadventure.ecs.component.PortalComponent
import com.github.quillraven.quillysadventure.ecs.component.RenderComponent
import com.github.quillraven.quillysadventure.ecs.component.StateComponent
import com.github.quillraven.quillysadventure.ecs.component.StatsComponent
import com.github.quillraven.quillysadventure.ecs.component.TakeDamageComponent
import com.github.quillraven.quillysadventure.ecs.component.TmxMapComponent
import com.github.quillraven.quillysadventure.ecs.component.TransformComponent
import com.github.quillraven.quillysadventure.ecs.component.TriggerComponent
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.map.MapType
import com.github.quillraven.quillysadventure.trigger.Trigger
import com.github.quillraven.quillysadventure.trigger.setupAfterBoss
import com.github.quillraven.quillysadventure.trigger.setupBossPitLeft
import com.github.quillraven.quillysadventure.trigger.setupBossPitRight
import com.github.quillraven.quillysadventure.trigger.setupBossTrigger
import com.github.quillraven.quillysadventure.trigger.setupSceneTrigger
import com.github.quillraven.quillysadventure.ui.FontType
import ktx.app.gdxError
import ktx.ashley.EngineEntity
import ktx.ashley.entity
import ktx.ashley.get
import ktx.ashley.with
import ktx.box2d.BodyDefinition
import ktx.box2d.FixtureDefinition
import ktx.box2d.body
import ktx.box2d.box
import ktx.box2d.chain
import ktx.box2d.loop
import ktx.log.logger

// float array to define the vertices of a loop shape for rectangle scenery objects
private val TMP_FLOAT_ARRAY = FloatArray(8) { 0f }
private val LOG = logger<Engine>()

fun Engine.getCharacter(type: Character, fill: Array<Entity>): Array<Entity> {
    fill.clear()
    entities.forEach {
        if (it[CharacterTypeComponent.mapper]?.type == type) {
            fill.add(it)
        }
    }
    return fill
}

// helper function to check if an entity is removed. This is needed for box2d contact listener because
// remove contact is triggered for entities that get removed and they should be ignored for the contacts events
fun Entity.isRemoved() = this.components.size() == 0

fun EngineEntity.withDefaultStaticPhysic(world: World, shape: Shape2D) {
    with<PhysicComponent> {
        body = world.body(BodyDef.BodyType.StaticBody) {
            userData = this@withDefaultStaticPhysic.entity
            fixedRotation = true
            shape2D(shape) {
                isSensor = true
                filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
            }
        }
    }
}

fun Engine.floatingText(
    posX: Float,
    posY: Float,
    fontType: FontType,
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
            this.fontType = fontType
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
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
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
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
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
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
                }

                // characters are not allowed to sleep because otherwise the damage emitter
                // sensors do not detect collision correctly (body needs to be awake to trigger
                // contact events)
                allowSleep = false

                // ground sensor to detect if entity can jump
                box(
                    cfg.size.x * 0.5f,
                    0.35f,
                    PhysicComponent.tmpVec2.set(0f + cfg.collBodyOffset.x, -cfg.size.y * 0.6f + cfg.collBodyOffset.y)
                ) {
                    userData = FIXTURE_TYPE_FOOT_SENSOR
                    this.isSensor = true
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
                }

                if (cfg.aggroRange > 0f) {
                    // character needs a sensor to detect if entities come within aggro range
                    box(2 * cfg.aggroRange, cfg.size.y) {
                        userData = FIXTURE_TYPE_AGGRO_SENSOR
                        isSensor = true
                        filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
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
        // character type
        with<CharacterTypeComponent> {
            type = cfg.characterType
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
                damageDelay = cfg.damageDelay
            }
        }
        // ability
        if (cfg.abilities.size > 0 || cfg.characterType == Character.PLAYER) {
            with<AbilityComponent> {
                cfg.abilities.forEach { addAbility(this@entity.entity, it) }
            }
        }
        // stats
        if (cfg.life > 0) {
            // this check is useful so that special NPCs like "Flippy" do not get
            // a StatsComponent that they do not need
            // this is useful for characters which do not fight
            with<StatsComponent> {
                this.armor = cfg.armor
                this.damage = cfg.damage
                this.life = cfg.life
                this.maxLife = this.life
                this.mana = cfg.mana
                this.maxMana = this.mana
                this.xp = cfg.xp
            }

            // characters can take damage from damage emitter entities
            with<TakeDamageComponent>()
        }
        // aggro
        if (cfg.aggroRange > 0f) {
            with<AggroComponent>()
        }

        // state
        if (cfg.defaultState != DefaultState.NONE) {
            with<StateComponent> {
                with(stateMachine) {
                    owner = this@entity.entity
                    if (cfg.life > 0) {
                        globalState = DefaultGlobalState.CHECK_ALIVE
                    }
                }
            }.stateMachine.changeState(cfg.defaultState)
        }
        // optional component data via lambda parameter
        this.compData()
    }
}

fun Engine.item(
    cfg: ItemCfg,
    world: World,
    posX: Float,
    posY: Float,
    compData: EngineEntity.() -> Unit = { Unit }
): Entity {
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
                    filter.categoryBits = FILTER_CATEGORY_ITEM
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
        // stats component to store the bonus
        with<StatsComponent> {
            life = cfg.lifeBonus.toFloat()
            mana = cfg.manaBonus.toFloat()
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.ITEM
        }

        this.compData()
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
                shape2D(shape) {
                    filter.categoryBits = FILTER_CATEGORY_SCENERY
                }
            }
        }
        // type of entity
        with<EntityTypeComponent> {
            this.type = EntityType.SCENERY
        }
    }
}

fun Engine.portalTarget(
    posX: Float,
    posY: Float,
    portalID: Int
): Entity {
    return this.entity {
        with<EntityTypeComponent> {
            this.type = EntityType.PORTAL
        }
        with<TransformComponent> {
            position.set(posX, posY)
            prevPosition.set(position)
            interpolatedPosition.set(position)
        }
        with<PortalComponent> {
            this.portalID = portalID
        }
        with<TmxMapComponent> {
            id = portalID
        }
    }
}

fun Engine.portal(
    world: World,
    shape: Shape2D,
    portalID: Int,
    active: Boolean,
    targetMap: MapType,
    targetPortal: Int,
    targetOffsetX: Int,
    flipParticleFX: Boolean
): Entity {
    return this.entity {
        // physic
        withDefaultStaticPhysic(world, shape)
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
        if (active) {
            // particle effect
            with<ParticleComponent> {
                type = ParticleAssets.PORTAL
                flipBy180Deg = flipParticleFX
            }
        }
        // portal information
        with<PortalComponent> {
            this.portalID = portalID
            this.active = active
            this.targetMap = targetMap
            this.targetOffsetX = targetOffsetX
            this.targetPortal = targetPortal
        }
        // tmx map info
        with<TmxMapComponent> {
            id = portalID
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
    source: Entity,
    damageDelay: Float = 0f
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
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
                }
            }
        }
        // collision
        with<CollisionComponent>()
        // type
        with<EntityTypeComponent> { type = EntityType.DAMAGE_EMITTER }
        // damage
        with<DealDamageComponent> {
            this.damage = damage
            this.lifeSpan = lifeSpan + damageDelay
            this.source = source
            this.damageDelay = damageDelay
        }
    }
}

fun Engine.globalLight(
    rayHandler: RayHandler,
    sunColor: Color,
    shadowAngle: Float
): Entity {
    return this.entity {
        // light
        with<LightComponent> {
            light = DirectionalLight(rayHandler, 128, sunColor, shadowAngle).apply {
                isSoft = false
                isStaticLight = false
            }
        }
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

fun Engine.missile(
    owner: Entity,
    world: World,
    spawnX: Float,
    spawnY: Float,
    width: Float,
    height: Float,
    speed: Float,
    lifeSpan: Float,
    damage: Float,
    particleEffect: ParticleAssets,
    flipBy180Deg: Boolean = false
): Entity {
    return this.entity {
        // physic
        with<PhysicComponent> {
            body = world.body(BodyDef.BodyType.DynamicBody) {
                position.set(spawnX + width * 0.5f, spawnY + height * 0.5f)
                userData = this@entity.entity
                // do not apply gravity to missiles
                gravityScale = 0f
                // damage emitters do not need to rotate
                fixedRotation = true
                box(width, height) {
                    isSensor = true
                    filter.categoryBits = FILTER_CATEGORY_GAME_OBJECT
                }
                linearVelocity.x = speed
            }
        }
        // transform
        with<TransformComponent> {
            position.set(spawnX, spawnY)
            prevPosition.set(position)
            interpolatedPosition.set(position)
            size.set(width, height)
        }
        // particle effect
        with<ParticleComponent> {
            this.type = particleEffect
            offsetX = width * 0.5f
            offsetY = height * 0.5f
            this.flipBy180Deg = flipBy180Deg
        }
        // collision
        with<CollisionComponent>()
        // type
        with<EntityTypeComponent> { type = EntityType.DAMAGE_EMITTER }
        // damage
        with<DealDamageComponent> {
            this.damage = damage
            this.lifeSpan = lifeSpan
            this.source = owner
        }
    }
}

fun Engine.trigger(
    triggerID: Int,
    triggerSetupFunctionName: String,
    reactOnCollision: Boolean,
    world: World,
    shape: Shape2D
): Entity {
    return this.entity {
        with<EntityTypeComponent> { type = EntityType.TRIGGER }
        // trigger component uses reflection which might fail if Tiled map object settings
        // are not correct (e.g. wrong classname or wrong package name)
        val newTrigger = Trigger.pool.obtain()
        with<TriggerComponent> { trigger = newTrigger }

        when (triggerSetupFunctionName) {
            "TriggerIntro.setupSceneTrigger" -> setupSceneTrigger(newTrigger)
            "TriggerMap2.setupBossTrigger" -> setupBossTrigger(newTrigger)
            "TriggerMap2.setupBossPitLeft" -> setupBossPitLeft(newTrigger)
            "TriggerMap2.setupBossPitRight" -> setupBossPitRight(newTrigger)
            "TriggerMap2.setupAfterBoss" -> setupAfterBoss(newTrigger)
            else -> gdxError("No trigger setup for name: $triggerSetupFunctionName")
        }

        if (reactOnCollision) {
            newTrigger.active = false
            withDefaultStaticPhysic(world, shape)
            with<CollisionComponent>()
        }

        with<TmxMapComponent> {
            id = triggerID
        }
    }
}

fun Engine.findPortal(portalID: Int, lambda: (Entity) -> Unit): Entity? {
    this.entities.forEach {
        it[PortalComponent.mapper]?.let { portal ->
            if (portal.portalID == portalID) {
                lambda(it)
                return it
            }
        }
    }

    return null
}
