package com.game.quillyjumper.map

import com.badlogic.ashley.core.Engine
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.maps.MapLayers
import com.badlogic.gdx.maps.MapObject
import com.badlogic.gdx.maps.objects.PolygonMapObject
import com.badlogic.gdx.maps.objects.PolylineMapObject
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.math.Shape2D
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.Array
import com.game.quillyjumper.UNIT_SCALE
import com.game.quillyjumper.configuration.Character
import com.game.quillyjumper.configuration.CharacterConfigurations
import com.game.quillyjumper.configuration.Item
import com.game.quillyjumper.configuration.ItemConfigurations
import com.game.quillyjumper.ecs.character
import com.game.quillyjumper.ecs.item
import ktx.log.logger
import ktx.math.vec2

private val LOG = logger<Map>()
private const val LAYER_PLAYER_SPAWN_LOCATION = "playerSpawnLocation"
private const val LAYER_COLLISION = "collision"
private const val LAYER_ENEMY = "enemy"
private const val LAYER_ITEM = "item"

class Map(val type: MapType, val tiledMap: TiledMap) {
    val playerStartLocation = vec2()
    val collisionShapes = Array<Shape2D>()

    companion object {
        val TMP_RCT_MAP_OBJECTS = Array<RectangleMapObject>()
        val TMP_POLYLINE_MAP_OBJECTS = Array<PolylineMapObject>()
        val TMP_POLYGON_MAP_OBJECTS = Array<PolygonMapObject>()
    }

    init {
        parsePlayerStartLocation()
        parseCollisionObjects()
        //TODO parse tiledmaplayers  and split them between foreground and background
        // rendersystem should then store backgroundlayers and foregroundlayers for rendering
        // to avoid looping through all layers all the time and check for the correct layer class type
    }

    private operator fun MapLayers.get(layerName: String): MapLayer? {
        val layer = this.get(layerName)
        if (layer == null) {
            // layer not defined in tiled map
            LOG.error { "There is no $layerName layer  for map $type" }
        }
        return layer
    }

    private val MapObject.id: Int
        get() = this.properties.get("id", -1, Int::class.java)

    private fun parsePlayerStartLocation() {
        tiledMap.layers[LAYER_PLAYER_SPAWN_LOCATION]?.run {
            // layer existing -> parse objects of layer
            this.objects.getByType(RectangleMapObject::class.java, TMP_RCT_MAP_OBJECTS)
            if (TMP_RCT_MAP_OBJECTS.size != 1) {
                // there is no or more than one player start location objects in the tiled map
                LOG.error { "There is not exactly one player start location defined for map $type. Amount: ${TMP_RCT_MAP_OBJECTS.size}" }
            } else {
                //  exactly one player start location defined -> store it
                playerStartLocation.run {
                    TMP_RCT_MAP_OBJECTS[0].rectangle.getPosition(this)
                    this.scl(UNIT_SCALE)
                }
            }
        }
    }

    private fun parseCollisionObjects() {
        tiledMap.layers[LAYER_COLLISION]?.run {
            // layer existing -> parse rectangle and polyline map objects
            // and add them to the collisionShapes array

            // 1) rectangles
            this.objects.getByType(RectangleMapObject::class.java, TMP_RCT_MAP_OBJECTS)
            TMP_RCT_MAP_OBJECTS.forEach { rectObj ->
                collisionShapes.add(rectObj.rectangle.apply {
                    x *= UNIT_SCALE
                    y *= UNIT_SCALE
                    width *= UNIT_SCALE
                    height *= UNIT_SCALE
                })
            }

            // 2) polyline
            this.objects.getByType(PolylineMapObject::class.java, TMP_POLYLINE_MAP_OBJECTS)
            TMP_POLYLINE_MAP_OBJECTS.forEach { polylineObj ->
                collisionShapes.add(polylineObj.polyline.apply {
                    setPosition(x * UNIT_SCALE, y * UNIT_SCALE)
                    vertices.forEachIndexed { index, vertex -> vertices[index] = vertex * UNIT_SCALE }
                })
            }

            // 3) polygon
            this.objects.getByType(PolygonMapObject::class.java, TMP_POLYGON_MAP_OBJECTS)
            TMP_POLYGON_MAP_OBJECTS.forEach { polygonObj ->
                collisionShapes.add(polygonObj.polygon.apply {
                    setPosition(x * UNIT_SCALE, y * UNIT_SCALE)
                    vertices.forEachIndexed { index, vertex -> vertices[index] = vertex * UNIT_SCALE }
                })
            }
        }
    }

    fun spawnEnemyObjects(engine: Engine, world: World, characterConfigurations: CharacterConfigurations) {
        tiledMap.layers[LAYER_ENEMY]?.run {
            // layer existing -> spawn enemies
            this.objects.getByType(RectangleMapObject::class.java, TMP_RCT_MAP_OBJECTS)
            TMP_RCT_MAP_OBJECTS.forEach { enemyObj ->
                try {
                    val charID = enemyObj.properties.get("Character", "", String::class.java)
                    val charKey = Character.valueOf(charID)
                    engine.character(
                        characterConfigurations[charKey],
                        world,
                        enemyObj.rectangle.x * UNIT_SCALE,
                        enemyObj.rectangle.y * UNIT_SCALE
                    )
                } catch (e: IllegalArgumentException) {
                    LOG.error(e) { "Invalid character property specified for object with ID ${enemyObj.id} for map $type" }
                }
            }
        }
    }

    fun spawnItemObjects(engine: Engine, world: World, itemConfigurations: ItemConfigurations) {
        tiledMap.layers[LAYER_ITEM]?.run {
            // layer existing -> spawn items
            this.objects.getByType(RectangleMapObject::class.java, TMP_RCT_MAP_OBJECTS)
            TMP_RCT_MAP_OBJECTS.forEach { itemObj ->
                try {
                    val itemID = itemObj.properties.get("Item", "", String::class.java)
                    val itemKey = Item.valueOf(itemID)
                    engine.item(
                        itemConfigurations[itemKey],
                        world,
                        itemObj.rectangle.x * UNIT_SCALE,
                        itemObj.rectangle.y * UNIT_SCALE
                    )
                } catch (e: IllegalArgumentException) {
                    LOG.error { "Invalid item property specified for object with ID ${itemObj.id} for map $type" }
                }
            }
        }
    }
}