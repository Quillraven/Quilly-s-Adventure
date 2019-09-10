package com.game.quillyjumper.map

import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.MapObjects
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Shape2D
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.assets.MapAssets
import com.game.quillyjumper.assets.MusicAssets
import ktx.log.logger

private val LOG = logger<Map>()

const val LAYER_PLAYER_SPAWN_LOCATION = "playerSpawnLocation"
const val LAYER_COLLISION = "collision"
const val LAYER_ENEMY = "enemy"
const val LAYER_ITEM = "item"
const val LAYER_PORTAL = "portal"

const val TILED_LAYER_BACKGROUND_PREFIX = "bgd"

const val PROPERTY_ID = "id"
const val PROPERTY_X = "x"
const val PROPERTY_Y = "y"
const val PROPERTY_WIDTH = "width"
const val PROPERTY_TILE_WIDTH = "tilewidth"
const val PROPERTY_HEIGHT = "height"
const val PROPERTY_TILE_HEIGHT = "tileheight"
const val PROPERTY_CHARACTER = "Character"
const val PROPERTY_ITEM = "Item"
const val PROPERTY_TARGET_MAP = "TargetMap"
const val PROPERTY_TARGET_OFFSET_X = "TargetOffsetX"
const val PROPERTY_TARGET_PORTAL_ID = "TargetPortalID"

// Map Types
enum class MapType(val asset: MapAssets, val music: MusicAssets) {
    MAP1(MapAssets.MAP_1, MusicAssets.LEVEL_1_REMASTERED),
    TEST_MAP(MapAssets.TEST_MAP, MusicAssets.LEVEL_1),
    TEST_MAP_SMALL(MapAssets.TEST_MAP_SMALL, MusicAssets.LEVEL_1_REMASTERED)
}

// extension method to access properties the Kotlin way ;)
inline fun <reified T> MapObject.property(key: String, defaultValue: T): T =
    this.properties[key, defaultValue, T::class.java]

// extension property to access shape of a MapObject
val MapObject.shape: Shape2D
    get() = when (this) {
        is RectangleMapObject -> this.rectangle
        is PolylineMapObject -> this.polyline
        is PolygonMapObject -> this.polygon
        else -> {
            LOG.error { "Unsupported MapObject of type ${this::class.java}. Cannot retrieve shape!" }
            Rectangle.tmp.set(0f, 0f, 1f, 1f)
        }
    }

// extension property to easily access MapObject IDs, x and y position
val MapObject.id: Int
    get() = this.property(PROPERTY_ID, -1)
val MapObject.x: Float
    get() = this.property(PROPERTY_X, 0f)
val MapObject.y: Float
    get() = this.property(PROPERTY_Y, 0f)

// extension method to access properties the Kotlin way for TiledMap
inline fun <reified T> TiledMap.property(key: String, defaultValue: T): T =
    this.properties[key, defaultValue, T::class.java]

class Map(val type: MapType, val tiledMap: TiledMap) {
    val width: Float
        get() = tiledMap.property(PROPERTY_WIDTH, 0f) * tiledMap.property(PROPERTY_TILE_WIDTH, 0f) * UNIT_SCALE
    val height: Float
        get() = tiledMap.property(PROPERTY_HEIGHT, 0f) * tiledMap.property(PROPERTY_TILE_HEIGHT, 0f) * UNIT_SCALE

    companion object {
        val defaultLayer = MapLayer()
    }

    private fun layer(layerName: String): MapLayer {
        val layer = tiledMap.layers.get(layerName)
        if (layer == null) {
            // layer not defined in tiled map
            LOG.debug { "There is no $layerName layer for map $type" }
            return defaultLayer
        }
        return layer
    }

    fun mapObjects(layerName: String): MapObjects = layer(layerName).objects
}