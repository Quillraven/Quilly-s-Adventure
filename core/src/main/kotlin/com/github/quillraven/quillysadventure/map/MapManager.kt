package com.github.quillraven.quillysadventure.map

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.quillysadventure.UNIT_SCALE
import com.github.quillraven.quillysadventure.configuration.Character
import com.github.quillraven.quillysadventure.configuration.CharacterConfigurations
import com.github.quillraven.quillysadventure.configuration.Item
import com.github.quillraven.quillysadventure.configuration.ItemConfigurations
import com.github.quillraven.quillysadventure.ecs.character
import com.github.quillraven.quillysadventure.ecs.component.EntityType
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.TmxMapComponent
import com.github.quillraven.quillysadventure.ecs.component.physicCmp
import com.github.quillraven.quillysadventure.ecs.component.playerCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.ecs.component.typeCmp
import com.github.quillraven.quillysadventure.ecs.globalLight
import com.github.quillraven.quillysadventure.ecs.item
import com.github.quillraven.quillysadventure.ecs.portal
import com.github.quillraven.quillysadventure.ecs.portalTarget
import com.github.quillraven.quillysadventure.ecs.scenery
import com.github.quillraven.quillysadventure.ecs.trigger
import com.github.quillraven.quillysadventure.event.GameEventManager
import ktx.ashley.get
import ktx.ashley.with
import ktx.log.logger
import ktx.tiled.id
import ktx.tiled.property
import ktx.tiled.shape
import ktx.tiled.type
import ktx.tiled.x
import ktx.tiled.y
import java.util.*

private val LOG = logger<MapManager>()

class MapManager(
    private val assets: AssetManager,
    private val world: World,
    private val rayHandler: RayHandler,
    private val ecsEngine: Engine,
    private val characterConfigurations: CharacterConfigurations,
    private val itemConfigurations: ItemConfigurations,
    private val playerEntities: ImmutableArray<Entity>,
    private val gameEventManager: GameEventManager
) {
    private var currentMapType = MapType.TEST_MAP
    private val mapCache = EnumMap<MapType, Map>(MapType::class.java)
    val mapEntityCache = EnumMap<MapType, MutableList<Int>>(MapType::class.java)

    fun currentMap() = currentMapType

    fun setMap(mapType: MapType, targetPortal: Int = -1, targetOffsetX: Int = -1) {
        LOG.debug { "Setting map $mapType" }
        val currentMap = mapCache[currentMapType]
        LOG.debug { "Current map is $currentMap" }
        if (currentMap != null) {
            storeMapEntities(currentMapType)
            LOG.debug { "Storing map entities of map $currentMapType: ${mapEntityCache[currentMapType]}" }

            gameEventManager.dispatchBeforeMapChangeEvent()
            // remove all non-player entities of the current loaded map
            ecsEngine.entities.forEach { entity ->
                if (entity.typeCmp.type != EntityType.PLAYER) {
                    // non player entity -> remove it
                    entity.add(ecsEngine.createComponent(RemoveComponent::class.java))
                }
            }
        }

        // check if new map is already existing. Otherwise, create it
        currentMapType = mapType
        mapCache.computeIfAbsent(mapType) { Map(mapType, assets[mapType.asset.filePath]) }.apply {
            LOG.debug { "Update player position with targetPortal=$targetPortal" }
            if (targetPortal == -1) {
                // target portal is not specified -> move to default player start location
                movePlayerToStartLocation(this)
            } else {
                // move player to target portal position
                movePlayerToPortal(this, targetPortal, targetOffsetX)
            }
            LOG.debug { "Create scenery" }
            createSceneryEntities(this)
            LOG.debug { "Create enemies" }
            createEnemyEntities(this)
            LOG.debug { "Create save points" }
            createSavepoints(this)
            LOG.debug { "Create NPCs" }
            createNPCs(this)
            LOG.debug { "Create items" }
            createItemEntities(this)
            LOG.debug { "Create portals" }
            createPortalEntities(this)
            LOG.debug { "Create triggers" }
            createTriggers(this)
            LOG.debug { "Create ambient light" }
            updateAmbientLight(this)
            LOG.debug { "Dispatch map change event" }
            gameEventManager.dispatchMapChangeEvent(this)
            LOG.debug { "setMap is done!" }
        }
    }

    fun storeMapEntities(mapType: MapType) {
        mapEntityCache.computeIfAbsent(mapType) { mutableListOf() }.apply {
            this.clear()
            ecsEngine.entities.forEach { entity ->
                val tmxMapCmp = entity[TmxMapComponent.mapper]
                if (tmxMapCmp != null) {
                    this.add(tmxMapCmp.id)
                }
            }
        }
    }

    fun storeMapEntities(mapType: MapType, entities: List<Int>) {
        mapEntityCache.computeIfAbsent(mapType) { mutableListOf() }.apply {
            this.clear()
            this.addAll(entities)
        }
    }

    fun movePlayer(x: Float, y: Float) {
        playerEntities.forEach { player ->
            player.physicCmp.body.run {
                setTransform(x, y, 0f)
                // also update transform component here as well because
                // otherwise the out of bounds system might trigger and sets the player
                // to a different location
                player.transfCmp.let { transform ->
                    transform.position.x = this.position.x - transform.size.x * 0.5f
                    transform.position.y = this.position.y - transform.size.y * 0.5f
                    transform.interpolatedPosition.set(transform.position)
                }
            }
            // set player checkpoint to map spawn location
            player.playerCmp.checkpoint.set(x, y)
        }
    }

    private fun movePlayerToStartLocation(map: Map) {
        movePlayer(map.startLocation.x, map.startLocation.y)
    }

    private fun movePlayerToPortal(map: Map, targetPortal: Int, targetOffsetX: Int) {
        map.forEachMapObject(LAYER_PORTAL) { mapObj ->
            if (mapObj.id == targetPortal) {
                // found target portal by ID -> move player to that location
                movePlayer(mapObj.x * UNIT_SCALE + targetOffsetX, mapObj.y * UNIT_SCALE)
                return
            }
        }

        // could not find portal -> move player to start location instead
        LOG.error { "Could not find portal $targetPortal for map ${map.type}" }
        movePlayerToStartLocation(map)
    }

    private fun createSceneryEntities(map: Map) {
        map.forEachMapObject(LAYER_COLLISION) { mapObj ->
            // loop through all map objects of that layer and
            // create the scenery entity according to the shape
            // of the map object.
            ecsEngine.scenery(world, mapObj.shape)
        }
    }

    private fun entityInCache(mapObject: MapObject, map: Map): Boolean {
        val entities = mapEntityCache[map.type]
        return entities == null || entities.contains(mapObject.id)
    }

    private fun createCharacterEntities(map: Map, layer: String) {
        map.forEachMapObject(layer) { mapObj ->
            if (!entityInCache(mapObj, map)) return@forEachMapObject

            try {
                val charKey = Character.valueOf(mapObj.name)
                ecsEngine.character(
                    characterConfigurations[charKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                ) {
                    with<TmxMapComponent> {
                        id = mapObj.id
                    }
                }
            } catch (e: IllegalArgumentException) {
                LOG.error(e) {
                    "Invalid name specified for object " +
                        "with ID ${mapObj.id} for map ${map.type} " +
                        "in layer $layer"
                }
            }
        }
    }

    private fun createEnemyEntities(map: Map) = createCharacterEntities(map, LAYER_ENEMY)

    private fun createSavepoints(map: Map) = createCharacterEntities(map, LAYER_SAVE_POINT)

    private fun createNPCs(map: Map) = createCharacterEntities(map, LAYER_NPC)

    private fun createItemEntities(map: Map) {
        map.forEachMapObject(LAYER_ITEM) { mapObj ->
            if (!entityInCache(mapObj, map)) return@forEachMapObject

            try {
                val itemKey = Item.valueOf(mapObj.name)
                ecsEngine.item(
                    itemConfigurations[itemKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                ) {
                    with<TmxMapComponent> {
                        id = mapObj.id
                    }
                }
            } catch (e: IllegalArgumentException) {
                LOG.error(e) {
                    "Invalid name specified for object " +
                        "with ID ${mapObj.id} for map ${map.type} " +
                        "in layer $LAYER_ITEM"
                }
            }
        }
    }


    private fun createPortalEntities(map: Map) {
        map.forEachMapObject(LAYER_PORTAL) { mapObj ->
            if (!entityInCache(mapObj, map)) return@forEachMapObject

            try {
                // retrieve and validate portal properties
                if (mapObj.type == "PortalTarget") {
                    ecsEngine.portalTarget(
                        mapObj.x * UNIT_SCALE,
                        mapObj.y * UNIT_SCALE,
                        mapObj.id
                    )
                } else {
                    val targetMap = MapType.valueOf(mapObj.property(PROPERTY_TARGET_MAP, ""))
                    val targetPortal = mapObj.property(PROPERTY_TARGET_PORTAL_ID, -1)
                    if (targetPortal == -1) {
                        LOG.error {
                            "Target portal ID not specified " +
                                "for object with ID ${mapObj.id} " +
                                "for map ${map.type}"
                        }
                        return@forEachMapObject
                    }
                    val targetOffsetX = mapObj.property(PROPERTY_TARGET_OFFSET_X, 0)
                    if (targetOffsetX == 0 && map.type != targetMap) {
                        LOG.error {
                            "Target offset X not specified " +
                                "for object with ID ${mapObj.id} " +
                                "for map ${map.type}"
                        }
                        return@forEachMapObject
                    }

                    // create portal entity
                    ecsEngine.portal(
                        world,
                        mapObj.shape,
                        mapObj.id,
                        mapObj.property(PROPERTY_PORTAL_ACTIVE, true),
                        targetMap,
                        targetPortal,
                        targetOffsetX,
                        mapObj.property(PROPERTY_FLIP_PARTICLE_FX, false)
                    )
                }
            } catch (e: IllegalArgumentException) {
                if (!mapObj.properties.containsKey(PROPERTY_TARGET_MAP)) {
                    LOG.error { "Missing target map property for object with ID ${mapObj.id} for map ${map.type}" }
                } else {
                    LOG.error(e) {
                        "Invalid map property specified " +
                            "for object with ID ${mapObj.id} " +
                            "for map ${map.type}"
                    }
                }
            }
        }
    }

    private fun createTriggers(map: Map) {
        map.forEachMapObject(LAYER_TRIGGER) { mapObj ->
            if (!entityInCache(mapObj, map)) return@forEachMapObject

            val triggerSetupFunction = mapObj.name
            if (triggerSetupFunction.isBlank()) {
                LOG.error {
                    "There is no trigger setup function defined " +
                        "for trigger ${mapObj.id} " +
                        "in map ${map.type}"
                }
                return@forEachMapObject
            } else if (!triggerSetupFunction.contains('.')) {
                LOG.error {
                    "Wrong trigger setup function definition " +
                        "for trigger ${mapObj.id} " +
                        "in map ${map.type}. " +
                        "Format is FILENAME.METHOD"
                }
                return@forEachMapObject
            }

            ecsEngine.trigger(
                mapObj.id,
                triggerSetupFunction,
                mapObj.property(PROPERTY_TRIGGER_REACT_ON_COLLISION, false),
                world,
                mapObj.shape
            )
        }
    }

    private fun updateAmbientLight(map: Map) {
        // set ambient light
        rayHandler.setAmbientLight(map.property(PROPERTY_AMBIENT, Color.BLACK))

        if (map.containsProperty(PROPERTY_SUN_COLOR)) {
            rayHandler.setBlur(true)
            rayHandler.setBlurNum(2)
            // create point light entity
            ecsEngine.globalLight(
                rayHandler,
                map.property(PROPERTY_SUN_COLOR, Color.WHITE),
                map.property(PROPERTY_SHADOW_ANGLE, 0f)
            )
        } else {
            rayHandler.setBlur(false)
        }
    }
}
