package com.github.quillraven.quillysadventure.ecs.system

import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.systems.SortedIteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.FloatArray
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.quillraven.quillysadventure.ShaderPrograms
import com.github.quillraven.quillysadventure.ShaderType
import com.github.quillraven.quillysadventure.ecs.component.ParticleComponent
import com.github.quillraven.quillysadventure.ecs.component.RemoveComponent
import com.github.quillraven.quillysadventure.ecs.component.RenderComponent
import com.github.quillraven.quillysadventure.ecs.component.TransformComponent
import com.github.quillraven.quillysadventure.ecs.component.particleCmp
import com.github.quillraven.quillysadventure.ecs.component.renderCmp
import com.github.quillraven.quillysadventure.ecs.component.transfCmp
import com.github.quillraven.quillysadventure.event.GameEventManager
import com.github.quillraven.quillysadventure.map.Map
import com.github.quillraven.quillysadventure.map.MapChangeListener
import com.github.quillraven.quillysadventure.map.PROPERTY_PARALLAX_VALUE
import com.github.quillraven.quillysadventure.map.TILED_LAYER_BACKGROUND_PREFIX
import ktx.ashley.allOf
import ktx.ashley.exclude
import ktx.graphics.use
import ktx.log.error
import ktx.log.logger
import ktx.math.vec2
import ktx.tiled.property

private val LOG = logger<RenderSystem>()

class RenderSystem(
    engine: Engine,
    private val gameEventManager: GameEventManager,
    private val batch: SpriteBatch,
    private val gameViewPort: Viewport,
    private val mapRenderer: OrthogonalTiledMapRenderer,
    private val shaderPrograms: ShaderPrograms
) : MapChangeListener, SortedIteratingSystem(
    allOf(RenderComponent::class, TransformComponent::class).exclude(RemoveComponent::class).get(),
    compareBy { entity -> entity.transfCmp }
) {
    private var vignetteActive = false
    private val resolutionVector = vec2()
    private var sepiaVignetteRadius = 0.1f

    private var colorActive = false
    var grayness = 1f
        set(value) {
            field = MathUtils.clamp(value, 0f, 1f)
        }

    private val camera = gameViewPort.camera as OrthographicCamera

    private val mapBackgroundLayers = Array<TiledMapTileLayer>()
    private val mapForegroundLayers = Array<TiledMapTileLayer>()
    private val mapParallaxValues = FloatArray()

    private val particleEffects =
        engine.getEntitiesFor(
            allOf(
                ParticleComponent::class,
                TransformComponent::class
            ).exclude(RemoveComponent::class).get()
        )

    override fun addedToEngine(engine: Engine?) {
        super.addedToEngine(engine)
        gameEventManager.addMapChangeListener(this)
    }

    override fun removedFromEngine(engine: Engine?) {
        super.removedFromEngine(engine)
        gameEventManager.removeMapChangeListener(this)
    }

    override fun update(deltaTime: Float) {
        // reset to original color in case the UI stage modified it
        batch.color = Color.WHITE
        // Update animation timer for animated tiles
        AnimatedTiledMapTile.updateAnimationBaseTime()
        // always sort entities before rendering
        forceSort()
        // update camera to set the correct matrix for rendering later on
        gameViewPort.apply()
        batch.use {
            if (vignetteActive) {
                // set mandatory uniforms for vignette effect
                resolutionVector.set(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
                batch.shader.setUniformf("resolution", resolutionVector)
                batch.shader.setUniformf("radius", sepiaVignetteRadius)
            } else if (colorActive) {
                // set mandatory uniforms for color effect
                batch.shader.setUniformf("grayness", grayness)
            }

            // set view of map renderer. Internally sets the projection matrix of the sprite batch
            // which is used to correctly render not map related stuff like our entities
            mapRenderer.setView(camera)
            // render background of map
            val numBgdLayers = mapBackgroundLayers.size
            val parallaxMinWidth = camera.viewportWidth * 0.5f
            for (i in 0 until numBgdLayers) {
                renderTileLayer(mapBackgroundLayers[i], i, parallaxMinWidth)
            }
            // render entities
            super.update(deltaTime)
            // render particle effects and reset blend state manually
            particleEffects.forEach { entity -> entity.particleCmp.effect.draw(it, deltaTime) }
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            // render foreground of map
            for (i in 0 until mapForegroundLayers.size) {
                renderTileLayer(mapForegroundLayers[i], i + numBgdLayers, parallaxMinWidth)
            }
        }
    }

    private fun renderTileLayer(layer: TiledMapTileLayer, parallaxIndex: Int, minWidth: Float) {
        val parallaxValue = mapParallaxValues[parallaxIndex]
        val camPos = camera.position
        if (parallaxValue == 0f || camPos.x <= minWidth) {
            // tile layer has no parallax value or minimum width is not yet reached to trigger
            // the parallax effect
            mapRenderer.renderTileLayer(layer)
        } else {
            // make parallax effect by drawing the layer offset to its original value and
            // therefore creating a sort of "move" effect for the user
            val origVal = camPos.x
            camPos.x += (minWidth - camPos.x) * parallaxValue
            camera.update()
            mapRenderer.setView(camera)
            mapRenderer.renderTileLayer(layer)
            // reset the camera to its original position to draw remaining stuff with original values
            camPos.x = origVal
            camera.update()
            mapRenderer.setView(camera)
        }
    }

    override fun processEntity(entity: Entity, deltaTime: Float) {
        entity.renderCmp.run {
            sprite.run {
                // if the sprite does not have any texture then do not render it to avoid null pointer exceptions
                if (texture == null) {
                    LOG.error { "Entity is without a texture for rendering" }
                    return
                }

                // adjust sprite position to render image centered around the entity's position
                val transform = entity.transfCmp
                setPosition(
                    transform.interpolatedPosition.x - (width - transform.size.x) * 0.5f,
                    transform.interpolatedPosition.y - 0.01f
                )
                draw(batch)
            }
        }
    }

    override fun mapChange(newMap: Map) {
        mapRenderer.map = newMap.tiledMap
        // retrieve background and foreground tiled map layers for rendering
        mapBackgroundLayers.clear()
        mapForegroundLayers.clear()
        mapParallaxValues.clear()
        mapRenderer.map.layers.forEach { layer ->
            if (layer is TiledMapTileLayer && layer.isVisible) {
                // tiled map layer which is visible for rendering
                // check if it is in the background or foreground
                if (layer.name.startsWith(TILED_LAYER_BACKGROUND_PREFIX)) {
                    mapBackgroundLayers.add(layer)
                } else {
                    mapForegroundLayers.add(layer)
                }
                mapParallaxValues.add(layer.property(PROPERTY_PARALLAX_VALUE, 0f))
            }
        }
    }

    fun setGrayScale() {
        batch.shader = shaderPrograms[ShaderType.GRAYSCALE]
        vignetteActive = false
        colorActive = false
    }

    fun setSepia(vignetteRadius: Float = 0.1f) {
        batch.shader = shaderPrograms[ShaderType.SEPIA]
        vignetteActive = true
        colorActive = false
        this.sepiaVignetteRadius = vignetteRadius
    }

    fun setNormalColor() {
        batch.shader = shaderPrograms[ShaderType.DEFAULT]
        colorActive = false
        vignetteActive = false
    }

    fun setColorShader(grayness: Float = 1f) {
        batch.shader = shaderPrograms[ShaderType.COLOR]
        colorActive = true
        this.grayness = grayness
    }
}
