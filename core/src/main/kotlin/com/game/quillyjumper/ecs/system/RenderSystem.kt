package com.game.quillyjumper.ecs.system

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.Viewport
import com.game.quillyjumper.ecs.component.RenderComponent
import com.game.quillyjumper.ecs.component.TransformComponent
import com.game.quillyjumper.map.Map
import com.game.quillyjumper.map.MapChangeListener
import ktx.ashley.allOf
import ktx.ashley.get
import ktx.graphics.use
import ktx.log.logger

private val LOG = logger<RenderSystem>()

class RenderSystem(
    private val batch: SpriteBatch,
    private val gameViewPort: Viewport,
    private val world: World,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val box2DDebugRenderer: Box2DDebugRenderer
) : MapChangeListener, SortedIteratingSystem(
    allOf(RenderComponent::class, TransformComponent::class).get(),
    compareBy { entity -> entity[TransformComponent.mapper] }
) {
    override fun update(deltaTime: Float) {
        // always sort entities before rendering
        forceSort()
        // update camera to set the correct matrix for rendering later on
        gameViewPort.apply()
        batch.use {
            // set view of map renderer internally sets the projection matrix of the sprite batch
            // which is used to correctly render not map related stuff like our entities
            mapRenderer.setView(gameViewPort.camera as OrthographicCamera)
            // render map
            if (mapRenderer.map != null) {
                AnimatedTiledMapTile.updateAnimationBaseTime()
                mapRenderer.map.layers.forEach { layer ->
                    if (layer is TiledMapTileLayer && layer.isVisible) {
                        mapRenderer.renderTileLayer(layer)
                    }
                }
            }
            // render entities
            super.update(deltaTime)
        }
        // debug render box2d
        box2DDebugRenderer.render(world, gameViewPort.camera.combined)
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity[RenderComponent.mapper]?.let { render ->
            entity[TransformComponent.mapper]?.let { transform ->
                // if the sprite does not have any texture then do not render it to avoid null pointer exceptions
                render.sprite.run {
                    if (texture == null) {
                        LOG.error { "Entity is without a texture for rendering" }
                        return
                    }

                    // adjust sprite position to render image centered around the entity's position
                    setPosition(
                        transform.interpolatedPosition.x - (width - transform.size.x) * 0.5f,
                        transform.interpolatedPosition.y - 0.01f
                    )
                    draw(batch)
                }
            }
        }
    }

    override fun mapChange(newMap: Map) {
        mapRenderer.map = newMap.tiledMap
    }
}