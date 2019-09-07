package com.game.quillyjumper.map

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.configuration.Item
import com.game.quillyjumper.configuration.ItemConfigurations
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.item
import com.game.quillyjumper.ecs.scenery
import com.game.quillyjumper.event.GameEventManager
import ktx.ashley.get
import ktx.log.logger
import java.util.*

private val LOG = logger<MapManager>()

class MapManager(
    private val assets: AssetManager,
    private val world: World,
    private val ecsEngine: Engine,
    private val characterConfigurations: CharacterConfigurations,
    private val itemConfigurations: ItemConfigurations,
    private val playerEntities: ImmutableArray<Entity>,
    private val gameEventManager: GameEventManager
) {
    private var currentMapType = MapType.TEST_MAP
    private val mapCache = EnumMap<MapType, Map>(MapType::class.java)

    fun setMap(mapType: MapType) {
        val currentMap = mapCache[currentMapType]
        if (currentMap != null) {
            //TODO
            // 1) save current entity data to a file
            // 2) remove current entities except for player entities
            // 3) check for new map if there is already existing data in the save file.
            //    Otherwise, load data from TiledMap.tmx file
        }

        // check if new map is already existing. Otherwise, create it
        currentMapType = mapType
        val newMap = mapCache.computeIfAbsent(mapType) { Map(mapType, assets[mapType.asset]) }.apply {
            movePlayerToStartLocation(this)
            createSceneryEntities(this)
            createEnemyEntities(this)
            createItemEntities(this)
            gameEventManager.dispatchMapChangeEvent(this)
        }
    }

    private fun movePlayerToStartLocation(map: Map) {
        val mapObjects = map.mapObjects(LAYER_PLAYER_SPAWN_LOCATION)
        if (mapObjects.count != 1) {
            LOG.error { "There is not exactly one player start location defined for map ${map.type}. Amount: ${mapObjects.count}" }
        } else {
            mapObjects.forEach { mapObj ->
                when (mapObj) {
                    is RectangleMapObject -> {
                        // use the first position that is available in the tiled map
                        // and move the player entities directly to that location
                        mapObj.rectangle.getPosition(PhysicComponent.tmpVec2)
                        // convert to world units
                        PhysicComponent.tmpVec2.scl(UNIT_SCALE)
                        // set player entity positions
                        playerEntities.forEach { player ->
                            player[PhysicComponent.mapper]?.body?.setTransform(PhysicComponent.tmpVec2, 0f)
                        }
                        return
                    }
                    else -> {
                        LOG.error { "Unsupported player start location map object type ${mapObj::class.java} for map ${map.type}" }
                    }
                }
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
}