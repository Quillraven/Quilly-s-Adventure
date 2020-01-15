package com.github.quillraven.quillyjumper.map

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.World
import com.github.quillraven.quillyjumper.UNIT_SCALE
import com.github.quillraven.quillyjumper.assets.get
import com.github.quillraven.quillyjumper.configuration.Character
import com.github.quillraven.quillyjumper.configuration.CharacterConfigurations
import com.github.quillraven.quillyjumper.configuration.Item
import com.github.quillraven.quillyjumper.configuration.ItemConfigurations
import com.github.quillraven.quillyjumper.ecs.*
import com.github.quillraven.quillyjumper.ecs.component.EntityType
import com.github.quillraven.quillyjumper.ecs.component.RemoveComponent
import com.github.quillraven.quillyjumper.ecs.component.physicCmp
import com.github.quillraven.quillyjumper.ecs.component.typeCmp
import com.github.quillraven.quillyjumper.event.GameEventManager
import ktx.log.logger
import ktx.tiled.*
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

    fun currentMap() = currentMapType

    fun setMap(mapType: MapType, targetPortal: Int = -1, targetOffsetX: Int = -1) {
        val currentMap = mapCache[currentMapType]
        if (currentMap != null) {
            //TODO
            // 1) save current entity data to a file
            // 2) check for new map if there is already existing data in the save file.
            //    Otherwise, load data from TiledMap.tmx file

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
        mapCache.computeIfAbsent(mapType) { Map(mapType, assets[mapType.asset]) }.apply {
            if (targetPortal == -1) {
                // target portal is not specified -> move to default player start location
                movePlayerToStartLocation(this)
            } else {
                // move player to target portal position
                movePlayerToPortal(this, targetPortal, targetOffsetX)
            }
            createSceneryEntities(this)
            createEnemyEntities(this)
            createSavepoints(this)
            createNPCs(this)
            createItemEntities(this)
            createPortalEntities(this)
            createTriggers(this)
            updateAmbientLight(this)
            gameEventManager.dispatchMapChangeEvent(this)
        }
    }


    private fun movePlayerToStartLocation(map: Map) {
        playerEntities.forEach { player ->
            player.physicCmp.body.setTransform(map.startLocation, 0f)
        }
    }

    private fun createSceneryEntities(map: Map) {
        map.forEachMapObject(LAYER_COLLISION) { mapObj ->
            // loop through all map objects of that layer and
            // create the scenery entity according to the shape
            // of the map object.
            ecsEngine.scenery(world, mapObj.shape)
        }
    }

    private fun createCharacterEntities(map: Map, layer: String) {
        map.forEachMapObject(layer) { mapObj ->
            try {
                val charKey = Character.valueOf(mapObj.name)
                ecsEngine.character(
                    characterConfigurations[charKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                )
            } catch (e: IllegalArgumentException) {
                LOG.error(e) { "Invalid name specified for object with ID ${mapObj.id} for map ${map.type} in layer $layer" }
            }
        }
    }

    private fun createEnemyEntities(map: Map) = createCharacterEntities(map, LAYER_ENEMY)

    private fun createSavepoints(map: Map) = createCharacterEntities(map, LAYER_SAVE_POINT)

    private fun createNPCs(map: Map) = createCharacterEntities(map, LAYER_NPC)

    private fun createItemEntities(map: Map) {
        map.forEachMapObject(LAYER_ITEM) { mapObj ->
            try {
                val itemKey = Item.valueOf(mapObj.name)
                ecsEngine.item(
                    itemConfigurations[itemKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                )
            } catch (e: IllegalArgumentException) {
                LOG.error(e) { "Invalid name specified for object with ID ${mapObj.id} for map ${map.type} in layer $LAYER_ITEM" }
            }
        }
    }

    private fun createPortalEntities(map: Map) {
        map.forEachMapObject(LAYER_PORTAL) { mapObj ->
            try {
                // retrieve and validate portal properties
                if (mapObj.type == "PortalTarget") {
                    ecsEngine.portalTarget(mapObj.x * UNIT_SCALE, mapObj.y * UNIT_SCALE, mapObj.id)
                } else {
                    val targetMap = MapType.valueOf(mapObj.property(PROPERTY_TARGET_MAP, ""))
                    val targetPortal = mapObj.property(PROPERTY_TARGET_PORTAL_ID, -1)
                    if (targetPortal == -1) {
                        LOG.error { "Target portal ID not specified for object with ID ${mapObj.id} for map ${map.type}" }
                        return@forEachMapObject
                    }
                    val targetOffsetX = mapObj.property(PROPERTY_TARGET_OFFSET_X, 0)
                    if (targetOffsetX == 0 && map.type != targetMap) {
                        LOG.error { "Target offset X not specified for object with ID ${mapObj.id} for map ${map.type}" }
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
                    LOG.error(e) { "Invalid map property specified for object with ID ${mapObj.id} for map ${map.type}" }
                }
            }
        }
    }


    private fun movePlayerToPortal(map: Map, targetPortal: Int, targetOffsetX: Int) {
        map.forEachMapObject(LAYER_PORTAL) { mapObj ->
            if (mapObj.id == targetPortal) {
                // found target portal by ID -> move player to that location
                playerEntities.forEach { player ->
                    player.physicCmp.body.setTransform(
                        mapObj.x * UNIT_SCALE + targetOffsetX,
                        mapObj.y * UNIT_SCALE,
                        0f
                    )
                }
                return
            }
        }

        // could not find portal -> move player to start location instead
        LOG.error { "Could not find portal $targetPortal for map ${map.type}" }
        movePlayerToStartLocation(map)
    }

    private fun createTriggers(map: Map) {
        map.forEachMapObject(LAYER_TRIGGER) { mapObj ->
            val triggerSetupFunction = mapObj.name
            if (triggerSetupFunction.isBlank()) {
                LOG.error { "There is no trigger setup function defined for trigger ${mapObj.id} in map ${map.type}" }
                return@forEachMapObject
            } else if (!triggerSetupFunction.contains('.')) {
                LOG.error { "Wrong trigger setup function definition for trigger ${mapObj.id} in map ${map.type}. Format is FILENAME.METHOD" }
                return@forEachMapObject
            }

            ecsEngine.trigger(
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
