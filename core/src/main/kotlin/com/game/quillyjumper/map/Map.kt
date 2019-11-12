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
import ktx.math.vec2

private val LOG = logger<Map>()

const val LAYER_PLAYER_SPAWN_LOCATION = "playerSpawnLocation"
const val LAYER_COLLISION = "collision"
const val LAYER_ENEMY = "enemy"
const val LAYER_SAVE_POINT = "savePoint"
const val LAYER_NPC = "npc"
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
const val PROPERTY_AMBIENT = "Ambient"
const val PROPERTY_SUN_COLOR = "SunColor"
const val PROPERTY_SHADOW_ANGLE = "ShadowAngle"
const val PROPERTY_PARALLAX_VALUE = "parallaxValue"
const val PROPERTY_FLIP_PARTICLE_FX = "flipParticleFX"

// Map Types
@Suppress("unused")
enum class MapType(val asset: MapAssets, val music: MusicAssets) {
    MAP1(MapAssets.MAP_1, MusicAssets.LEVEL_1_REMASTERED),
    MAP2(MapAssets.MAP_2, MusicAssets.LEVEL_2),
    TEST_MAP(MapAssets.TEST_MAP, MusicAssets.LEVEL_1)
}

// extension method to access properties the Kotlin way ;)
inline fun <reified T> MapObject.property(key: String, defaultValue: T): T =
    this.properties[key, defaultValue, T::class.java]

inline fun <reified T> MapLayer.property(key: String, defaultValue: T): T =
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

class Map(val type: MapType, val tiledMap: TiledMap) {
    val width: Float
        get() = property(PROPERTY_WIDTH, 0f) * property(PROPERTY_TILE_WIDTH, 0f) * UNIT_SCALE
    val height: Float
        get() = property(PROPERTY_HEIGHT, 0f) * property(PROPERTY_TILE_HEIGHT, 0f) * UNIT_SCALE
    val startLocation = vec2()

    companion object {
        val defaultLayer = MapLayer()
    }

    init {
        // parse player start location and store it because it is used in several locations
        // like e.g. MapManager.changeMap or OutOfBoundsSystem
        val mapObjects = mapObjects(LAYER_PLAYER_SPAWN_LOCATION)
        if (mapObjects.count != 1) {
            LOG.error { "There is not exactly one player start location defined for map ${type}. Amount: ${mapObjects.count}" }
        } else {
            with(mapObjects.first()) { startLocation.set(x * UNIT_SCALE, y * UNIT_SCALE) }
        }
    }

    // extension method to access properties the Kotlin way for TiledMap
    inline fun <reified T> property(key: String, defaultValue: T): T =
        tiledMap.properties[key, defaultValue, T::class.java]

    fun containsProperty(key: String) = tiledMap.properties.containsKey(key)

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