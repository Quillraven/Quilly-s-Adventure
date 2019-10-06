package com.game.quillyjumper.map

import box2dLight.RayHandler
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.configuration.Item
import com.game.quillyjumper.configuration.ItemConfigurations
import com.game.quillyjumper.ecs.*
import com.game.quillyjumper.ecs.component.EntityType
import com.game.quillyjumper.ecs.component.RemoveComponent
import com.game.quillyjumper.ecs.component.physicCmp
import com.game.quillyjumper.ecs.component.typeCmp
import com.game.quillyjumper.event.GameEventManager
import ktx.log.logger
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
            createItemEntities(this)
            createPortalEntities(this)
            updateAmbientLight(this)
            gameEventManager.dispatchMapChangeEvent(this)
        }
    }

    private fun movePlayerToStartLocation(map: Map) {
        val mapObjects = map.mapObjects(LAYER_PLAYER_SPAWN_LOCATION)
        if (mapObjects.count != 1) {
            LOG.error { "There is not exactly one player start location defined for map ${map.type}. Amount: ${mapObjects.count}" }
        } else {
            mapObjects.forEach { mapObj ->
                // set player entity positions to first location of tiled map
                playerEntities.forEach { player ->
                    player.physicCmp.body.setTransform(
                        mapObj.x * UNIT_SCALE,
                        mapObj.y * UNIT_SCALE,
                        0f
                    )
                }
                return
            }
        }
    }

    private fun createSceneryEntities(map: Map) {
        map.mapObjects(LAYER_COLLISION).forEach { mapObj ->
            // loop through all map objects of that layer and
            // create the scenery entity according to the shape
            // of the map object.
            ecsEngine.scenery(world, mapObj.shape)
        }
    }

    private fun createEnemyEntities(map: Map) {
        map.mapObjects(LAYER_ENEMY).forEach { mapObj ->
            try {
                val charKey = Character.valueOf(mapObj.property(PROPERTY_CHARACTER, ""))
                ecsEngine.character(
                    characterConfigurations[charKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                )
            } catch (e: IllegalArgumentException) {
                if (!mapObj.properties.containsKey(PROPERTY_CHARACTER)) {
                    LOG.error { "Missing character property for object with ID ${mapObj.id} for map ${map.type}" }
                } else {
                    LOG.error(e) { "Invalid character property specified for object with ID ${mapObj.id} for map ${map.type}" }
                }
            }
        }
    }

    private fun createItemEntities(map: Map) {
        map.mapObjects(LAYER_ITEM).forEach { mapObj ->
            try {
                val itemKey = Item.valueOf(mapObj.property(PROPERTY_ITEM, ""))
                ecsEngine.item(
                    itemConfigurations[itemKey],
                    world,
                    mapObj.x * UNIT_SCALE,
                    mapObj.y * UNIT_SCALE
                )
            } catch (e: IllegalArgumentException) {
                if (!mapObj.properties.containsKey(PROPERTY_ITEM)) {
                    LOG.error { "Missing item property for object with ID ${mapObj.id} for map ${map.type}" }
                } else {
                    LOG.error(e) { "Invalid item property specified for object with ID ${mapObj.id} for map ${map.type}" }
                }
            }
        }
    }

    private fun createPortalEntities(map: Map) {
        map.mapObjects(LAYER_PORTAL).forEach { mapObj ->
            try {
                // retrieve and validate portal properties
                val targetMap = MapType.valueOf(mapObj.property(PROPERTY_TARGET_MAP, ""))
                val targetPortal = mapObj.property(PROPERTY_TARGET_PORTAL_ID, -1)
                if (targetPortal == -1) {
                    LOG.error { "Target portal ID not specified for object with ID ${mapObj.id} for map ${map.type}" }
                    return@forEach
                }
                val targetOffsetX = mapObj.property(PROPERTY_TARGET_OFFSET_X, 0)
                if (targetOffsetX == 0) {
                    LOG.error { "Target offset X not specified for object with ID ${mapObj.id} for map ${map.type}" }
                    return@forEach
                }

                // create portal entity
                ecsEngine.portal(world, mapObj.shape, targetMap, targetPortal, targetOffsetX)
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
        map.mapObjects(LAYER_PORTAL).forEach { mapObj ->
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