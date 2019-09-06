package com.game.quillyjumper.map

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.utils.ImmutableArray
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.math.Polyline
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.physics.box2d.World
import com.game.quillyjumper.assets.MapAssets
import com.game.quillyjumper.assets.MusicAssets
import com.game.quillyjumper.assets.get
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.configuration.ItemConfigurations
import com.game.quillyjumper.ecs.component.PhysicComponent
import com.game.quillyjumper.ecs.scenery
import com.game.quillyjumper.event.GameEventManager
import ktx.ashley.get
import java.util.*

enum class MapType(val asset: MapAssets, val music: MusicAssets) {
    TEST_MAP(MapAssets.TEST_MAP, MusicAssets.LEVEL_1)
}

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
            //TODO cleanup entities from current map
        }

        // check if new map is already existing. Otherwise, create it
        currentMapType = mapType
        val newMap = mapCache.computeIfAbsent(mapType) { Map(mapType, assets[mapType.asset]) }
        movePlayerToStartLocation(newMap)
        createCollisionEntities(newMap)
        newMap.spawnEnemyObjects(ecsEngine, world, characterConfigurations)
        newMap.spawnItemObjects(ecsEngine, world, itemConfigurations)
        gameEventManager.dispatchMapChangeEvent(newMap)
    }

    private fun movePlayerToStartLocation(map: Map) {
        playerEntities.forEach { player ->
            player[PhysicComponent.mapper]?.body?.setTransform(map.playerStartLocation, 0f)
        }
    }

    private fun createCollisionEntities(map: Map) {
        map.collisionShapes.forEach { shape ->
            when (shape) {
                is Rectangle -> ecsEngine.scenery(world, shape.x, shape.y, shape.width, shape.height)
                is Polyline -> ecsEngine.scenery(world, shape.x, shape.y, shape.vertices, loop = false)
                is Polygon -> ecsEngine.scenery(world, shape.x, shape.y, shape.vertices, loop = true)
            }
        }
    }
}