package com.github.quillraven.quillysadventure.map

import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.github.quillraven.quillysadventure.UNIT_SCALE
import com.github.quillraven.quillysadventure.assets.MapAssets
import com.github.quillraven.quillysadventure.assets.MusicAssets
import ktx.log.error
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.containsProperty
import ktx.tiled.forEachMapObject
import ktx.tiled.layer
import ktx.tiled.property
import ktx.tiled.totalHeight
import ktx.tiled.totalWidth
import ktx.tiled.x
import ktx.tiled.y

private val LOG = logger<Map>()

const val LAYER_PLAYER_SPAWN_LOCATION = "playerSpawnLocation"
const val LAYER_COLLISION = "collision"
const val LAYER_ENEMY = "enemy"
const val LAYER_SAVE_POINT = "savePoint"
const val LAYER_NPC = "npc"
const val LAYER_ITEM = "item"
const val LAYER_PORTAL = "portal"
const val LAYER_TRIGGER = "trigger"

const val TILED_LAYER_BACKGROUND_PREFIX = "bgd"

const val PROPERTY_TARGET_MAP = "TargetMap"
const val PROPERTY_TARGET_OFFSET_X = "TargetOffsetX"
const val PROPERTY_TARGET_PORTAL_ID = "TargetPortalID"
const val PROPERTY_AMBIENT = "Ambient"
const val PROPERTY_SUN_COLOR = "SunColor"
const val PROPERTY_SHADOW_ANGLE = "ShadowAngle"
const val PROPERTY_PARALLAX_VALUE = "ParallaxValue"
const val PROPERTY_FLIP_PARTICLE_FX = "FlipParticleFX"
const val PROPERTY_TRIGGER_REACT_ON_COLLISION = "TriggerReactOnCollision"
const val PROPERTY_PORTAL_ACTIVE = "Active"

// Map Types
@Suppress("unused")
enum class MapType(val asset: MapAssets, val music: MusicAssets) {
    INTRO(MapAssets.INTRO, MusicAssets.LEVEL_1_REMASTERED),
    MAP1(MapAssets.MAP_1, MusicAssets.LEVEL_1_REMASTERED),
    MAP2(MapAssets.MAP_2, MusicAssets.LEVEL_2),
    MAIN_MENU(MapAssets.MAIN_MENU, MusicAssets.MENU),
    GAME_OVER(MapAssets.GAME_OVER, MusicAssets.GAME_OVER),
    TEST_MAP(MapAssets.TEST_MAP, MusicAssets.GAME_OVER)
}

class Map(val type: MapType, val tiledMap: TiledMap) {
    val startLocation = vec2()
    val width = tiledMap.totalWidth() * UNIT_SCALE
    val height = tiledMap.totalHeight() * UNIT_SCALE

    init {
        // parse player start location and store it because it is used in several locations
        // like e.g. MapManager.changeMap or OutOfBoundsSystem
        val mapObjects = tiledMap.layer(LAYER_PLAYER_SPAWN_LOCATION).objects
        if (mapObjects.count != 1) {
            LOG.error {
                "There is not exactly one player start location " +
                        "defined for map $type. Amount: ${mapObjects.count}"
            }
        } else {
            with(mapObjects.first()) { startLocation.set(x * UNIT_SCALE, y * UNIT_SCALE) }
        }
    }

    inline fun forEachMapObject(layerName: String, action: (MapObject) -> Unit) =
        tiledMap.forEachMapObject(layerName, action)

    inline fun <reified T> property(key: String, defaultValue: T): T = tiledMap.property(key, defaultValue)

    fun containsProperty(key: String) = tiledMap.containsProperty(key)
}
